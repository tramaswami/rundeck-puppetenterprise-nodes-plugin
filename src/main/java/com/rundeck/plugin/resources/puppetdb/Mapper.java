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

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.common.NodeEntryImpl;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.rundeck.plugin.resources.puppetdb.client.model.PuppetDBNode;

/**
 *
 */
public class Mapper implements Constants {

    private static Logger log = Logger.getLogger(ResourceModelFactory.class);

    private final Properties properties;
    private final PropertyUtilsBean propertyUtilsBean;

    public Mapper(final Properties properties) {
        this.properties = properties;
        this.propertyUtilsBean = new PropertyUtilsBean();
    }

    public Optional<INodeEntry> apply(final PuppetDBNode puppetNode,
            final Map<String, Object> mappings) {

        // create a new instance
        final NodeEntryImpl result = newNodeTreeImpl();

        // parse every property BUT tags and attributes
        try {
            final Set<String> especialProperties = setOf("tags", "attributes");
            final Map<String, String> readProperties = assembleMapOmitingKeys(puppetNode, mappings, especialProperties);
            propertyUtilsBean.copyProperties(result, readProperties);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.warn("while trying to assemble Rundeck node from PuppetDB Node", e);
        }

        // parse attributes
        final boolean hasAttributes = mappings.containsKey("attributes");
        if (hasAttributes) {
            final Object attributesMapping = mappings.get("attributes");

            final boolean isValidMapping = null != attributesMapping && attributesMapping instanceof Map;
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

            // add default tag if any
            final Optional<String> defaultNodeTag = getDefaultNodeTag();
            if (defaultNodeTag.isPresent()) {
                result.getTags().add(defaultNodeTag.get());
            }

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
        return isNodeInValidState(result) ? Optional.<INodeEntry> of(result) : Optional.<INodeEntry> absent();
    }

    final Optional<String> getDefaultNodeTag() {
        final String value = properties.getProperty(PROPERTY_DEFAULT_NODE_TAG, "");
        if (isBlank(value)) {
            log.warn(format("Property: '%s' is set, but empty. Will be ignored", PROPERTY_DEFAULT_NODE_TAG));
            return Optional.absent();
        }

        return Optional.of(value);
    }

    public <T> Set<T> setOf(T... ts) {
        return new LinkedHashSet<>(asList(ts));
    }

    private Map<String, String> assembleMap(final PuppetDBNode puppetNode,
            final Map<String, Object> mappings) {
        final Map<String, String> result = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : mappings.entrySet()) {
            final String key = entry.getKey();

            final boolean isValidMapping = entry.getValue() instanceof Map;
            if (!isValidMapping) {
                log.warn("wrong mapping for property: 'key', please specify a json object with properties 'path' and/or 'default'");
                continue;
            }

            final Map<String, String> mapping = (Map<String, String>) entry.getValue();
            final String value = getPuppetNodeProperty(puppetNode, mapping);
            if (null != value) { 
                result.put(key, value);
            }
        }

        return result;
    }

    private Map<String, String> assembleMapOmitingKeys(final PuppetDBNode puppetNode,
            final Map<String, Object> mappings,
            final Set<String> omitKeys) {
        final Map<String, Object> newMappings = new LinkedHashMap<>(mappings);

        for (final String key : omitKeys) {
            newMappings.remove(key);
        }

        return assembleMap(puppetNode, newMappings);
    }

    private String getPuppetNodeProperty(final PuppetDBNode puppetNode,
            final Map<String, String> propertyMapping) {
        if (null == propertyMapping || propertyMapping.isEmpty()) {
            return "";
        }

        final boolean isPath = propertyMapping.containsKey("path");
        if (isPath) {
            final String value = getPuppetNodeProperty(puppetNode, propertyMapping.get("path"));
            if (isNotBlank(value)) {
                return value;
            }
        }

        final boolean isDefault = propertyMapping.containsKey("default");
        if (isDefault) {
            final String value = propertyMapping.get("default");
            if (isNotBlank(value)) {
                return value;
            }
        }

        return "";
    }

    private String getPuppetNodeProperty(final PuppetDBNode puppetNode,
            final String propertyPath) {
        if (isBlank(propertyPath) || null == puppetNode) {
            return "";
        }

        try {
            final Object value = propertyUtilsBean.getProperty(puppetNode, propertyPath);
            return null == value ? "" : value.toString();
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | NestedNullException e) {
            final String template = "can't parse propertyPath: '%s'";
            final String message = format(template, propertyPath);
            log.warn(message, e);
        }

        return "";
    }

    private NodeEntryImpl newNodeTreeImpl() {
        final NodeEntryImpl result = new NodeEntryImpl();

        if (null == result.getTags()) {
            result.setTags(new LinkedHashSet<>());
        }

        if (null == result.getAttributes()) {
            result.setAttributes(new LinkedHashMap<String, String>());
        }

        return result;
    }

    protected final boolean isNodeInValidState(final INodeEntry nodeEntry) {
        if (null == nodeEntry) {
            log.error("invalid node state, node is null");
            return false;
        }

        final String nodename = nodeEntry.getNodename();
        if (isBlank(nodename)) {
            log.warn("invalid node state, nodename is blank");
            return false;
        }

        final String hostname = nodeEntry.getHostname();
        if (isBlank(hostname)) {
            log.warn("invalid node state, hostname is blank");
            return false;
        }

        final String username = nodeEntry.getUsername();
        if (isBlank(username)) {
            log.warn("ignoring parsed node, username is blank");
            return false;
        }

        final Set<?> tags = nodeEntry.getTags();
        if (CollectionUtils.isEmpty(tags)) {
            log.warn(format("ignoring parsed node, tags is empty. use plugin property %s to solve this", PROPERTY_DEFAULT_NODE_TAG));
            return false;
        }

        return true;
    }

    public Function<PuppetDBNode, Optional<INodeEntry>> withFixedMapping(final Map<String, Object> mapping) {
        final Mapper mapper = this;
        return new Function<PuppetDBNode, Optional<INodeEntry>>() {
            @Override
            public Optional<INodeEntry> apply(final PuppetDBNode input) {
                return mapper.apply(input, mapping);
            }
        };
    }

}
