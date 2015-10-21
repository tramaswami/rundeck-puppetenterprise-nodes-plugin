/*
 * Copyright 2011 DTO Solutions, Inc. (http://dtosolutions.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
* NodeGenerator.java
* 
* User: Greg Schueler <a href="mailto:greg@dtosolutions.com">greg@dtosolutions.com</a>
* Created: Oct 18, 2010 7:03:37 PM
* 
*/
package com.rundeck.plugin.resources.puppetdb;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.common.NodeEntryImpl;
import com.rundeck.plugin.resources.puppetdb.client.model.PuppetDBNode;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 *
 */
public class Mapper implements BiFunction<PuppetDBNode, Map<String, Object>, Optional<INodeEntry>> {

    private static Logger log = Logger.getLogger(ResourceModelFactory.class);

    private final PropertyUtilsBean propertyUtilsBean;

    public Mapper() {
        this.propertyUtilsBean = new PropertyUtilsBean();
    }

    @Override
    public Optional<INodeEntry> apply(final PuppetDBNode puppetNode,
                                      final Map<String, Object> mappings) {
        // create a new instance
        final NodeEntryImpl result = newNodeTreeImpl();

        // parse every property BUT tags and attributes
        try {
            final Set<String> especialProperties = setOf("tags", "attributes");
            final Map<String, String> readProperties = assembleMapOmitingKeys(puppetNode, mappings, especialProperties);
            propertyUtilsBean.copyProperties(result, readProperties);
        } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
            log.warn("while trying to assemble Rundeck node from PuppetDB Node", e);
        }

        // parse attributes
        final boolean hasAttributes = mappings.containsKey("attributes");
        if (hasAttributes) {
            final Object attributesMapping = mappings.getOrDefault("attributes", emptyMap());

            final boolean isValidMapping = attributesMapping instanceof Map;
            if (isValidMapping) {
                final Map<String, String> newAttributes = assembleMap(puppetNode, (Map<String, Object>) attributesMapping);
                result.getAttributes().putAll(newAttributes);
            }
        }

        // parse tags
        final boolean hasTags = mappings.containsKey("tags");
        if (hasTags) {
            // TODO: for now, tags is every tag we found.
            result.getTags().addAll(puppetNode.getClasses());

            /*
            final Object tagsMapping = mappings.getOrDefault("tags", emptyMap());
            final boolean isValidMapping = tagsMapping instanceof List;
            if (isValidMapping) {
                final Set<String> newTags = assembleSet(puppetNode, (List<Map<String, String>>) tagsMapping);
                result.getTags().addAll(newTags);
            }
            */

        }

        // check if valid
        return validState(result) ? Optional.of(result) : empty();
    }

    private Set<String> assembleSet(final PuppetDBNode puppetNode,
                                    final List<Map<String, String>> mappings) {
        return mappings.stream()
                .map(propertyMapping -> getPuppetNodeProperty(puppetNode, propertyMapping))
                .filter(StringUtils::isNotBlank)
                .collect(toSet());
    }

    public <T> Set<T> setOf(T... ts) {
        return new LinkedHashSet<>(asList(ts));
    }

    private Map<String, String> assembleMap(final PuppetDBNode puppetNode,
                                            final Map<String, Object> mappings) {
        final Map<String, String> result = new LinkedHashMap<>();

        mappings.forEach((final String key, final Object _mapping) -> {
            final boolean isValidMapping = _mapping instanceof Map;
            if (!isValidMapping) {
                log.warn("wrong mapping for property: 'key', please specify a json object with properties 'path' and/or 'default'");
                return;
            }

            final Map<String, String> mapping = (Map<String, String>) _mapping;
            final String value = getPuppetNodeProperty(puppetNode, mapping);
            if (isNotBlank(value)) {
                result.put(key, value);
            }
        });

        return result;
    }

    private Map<String, String> assembleMapOmitingKeys(final PuppetDBNode puppetNode,
                                                       final Map<String, Object> mappings,
                                                       final Set<String> omitKeys) {
        final Map<String, Object> newMappings = new LinkedHashMap<>(mappings);
        omitKeys.forEach(newMappings::remove);
        return assembleMap(puppetNode, newMappings);
    }

    private String getPuppetNodeProperty(final PuppetDBNode puppetNode,
                                         final Map<String, String> propertyMapping) {
        if (isNull(propertyMapping) || propertyMapping.isEmpty()) {
            return "";
        }

        final boolean isPath = propertyMapping.containsKey("path");
        if (isPath) {
            final String value = getPuppetNodeProperty(puppetNode, propertyMapping.getOrDefault("path", ""));
            if (isNotBlank(value)) {
                return value;
            }
        }

        final boolean isDefault = propertyMapping.containsKey("default");
        if (isDefault) {
            final String value = propertyMapping.getOrDefault("default", "");
            if (isNotBlank(value)) {
                return value;
            }
        }

        return "";
    }

    private String getPuppetNodeProperty(final PuppetDBNode puppetNode,
                                         final String propertyPath) {
        if (isBlank(propertyPath)) {
            return "";
        }

        try {
            final Object value = propertyUtilsBean.getProperty(puppetNode, propertyPath);

            return isNull(value) ? "" : value.toString();
        } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
            final String template = "can't parse propertyPath: '%s'";
            final String message = format(template, propertyPath);
            log.warn(message, e);
        }

        return "";
    }

    private NodeEntryImpl newNodeTreeImpl() {
        final NodeEntryImpl result = new NodeEntryImpl();

        if (isNull(result.getTags())) {
            result.setTags(new LinkedHashSet<>());
        }

        if (isNull(result.getAttributes())) {
            result.setAttributes(new LinkedHashMap<>());
        }

        return result;
    }

    private boolean validState(final INodeEntry nodeEntry) {
        if (isNull(nodeEntry)) {
            return false;
        }

        final String nodename = nodeEntry.getNodename();
        final String hostname = nodeEntry.getHostname();
        final String username = nodeEntry.getUsername();
        final Set tags = nodeEntry.getTags();

        return isNotBlank(nodename) &&
                isNotBlank(hostname) &&
                isNotBlank(username) &&
                !isEmpty(tags);
    }

    private boolean isEmpty(final Set<?> tags) {
        if (null == tags) {
            return true;
        }

        if (tags.isEmpty()) {
            return true;
        }

        return !tags.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .isPresent();
    }

}
