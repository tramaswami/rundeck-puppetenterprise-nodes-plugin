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
* PuppetEnterpriseResourceModelSource.java
* 
* User: Greg Schueler <a href="mailto:greg@dtosolutions.com">greg@dtosolutions.com</a>
* Created: 9/1/11 4:27 PM
* 
*/
package com.rundeck.plugin.resources.puppetdb;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import com.dtolabs.rundeck.core.common.Framework;
import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.common.INodeSet;
import com.dtolabs.rundeck.core.common.NodeSetImpl;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.core.plugins.configuration.Property;
import com.dtolabs.rundeck.core.resources.ResourceModelSource;
import com.dtolabs.rundeck.core.resources.ResourceModelSourceException;
import com.dtolabs.rundeck.core.resources.ResourceModelSourceFactory;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rundeck.plugin.resources.puppetdb.client.DefaultPuppetAPI;
import com.rundeck.plugin.resources.puppetdb.client.PuppetAPI;
import com.rundeck.plugin.resources.puppetdb.client.model.Node;
import com.rundeck.plugin.resources.puppetdb.client.model.PuppetDBNode;
import org.apache.log4j.Logger;

@Plugin(name = "puppet-enterprise", service = "ResourceModelSource")
public class ResourceModelFactory implements ResourceModelSourceFactory, Describable, Constants {

    private static Logger log = Logger.getLogger(ResourceModelFactory.class);

    private Framework framework;
    private Gson gson;

    public ResourceModelFactory(final Framework framework) {
        this.framework = framework;
        this.gson = new Gson();
    }

    @Override
    public ResourceModelSource createResourceModelSource(final Properties properties) throws ConfigurationException {
        final PuppetAPI puppetAPI = getPuppetAPI(properties);
        final Mapper mapper = new Mapper();
        final Map<String, Object> mapping = getMapping(properties);

        return new ResourceModelSource() {
            @Override
            public INodeSet getNodes() throws ResourceModelSourceException {
                // get list of nodes without filtering
                final List<Node> nodes = puppetAPI.getNodes();
                if (null == nodes || nodes.isEmpty()) {
                    return new NodeSetImpl();
                }

                // build nodes with facts and tags attached
                final List<PuppetDBNode> puppetNodes = FluentIterable.from(nodes)
                        .transform(puppetAPI.queryNode())
                        .toList();

                final List<INodeEntry> rundeckNodes = FluentIterable.from(puppetNodes)
                        .transform(mapper.withFixedMapping(mapping))
                        .filter(new Predicate<Optional<INodeEntry>>() {
                            @Override
                            public boolean apply(final Optional<INodeEntry> input) {
                                return input.isPresent();
                            }
                        })
                        .transform(new Function<Optional<INodeEntry>, INodeEntry>() {
                            @Override
                            public INodeEntry apply(final Optional<INodeEntry> input) {
                                return input.get();
                            }
                        })
                        .toList();

                final NodeSetImpl nodeSet = new NodeSetImpl();
                nodeSet.putNodes(rundeckNodes);
                return nodeSet;
            }
        };
    }

    @Override
    public Description getDescription() {
        return PLUGIN_DESCRIPTION;
    }

    public PuppetAPI getPuppetAPI(final Properties properties) throws ConfigurationException {
        final List<Property> missingProperties = getMissingProperties(properties);
        if (isNotEmpty(missingProperties)) {
            final String missingPropertiesNames = joinPropertyNames(missingProperties);

            final String template = "Can't start %s plugin, Missing properties: '%s'";
            final String message = format(template, PROVIDER_NAME, missingPropertiesNames);
            throw new ConfigurationException(message);
        }


        return new DefaultPuppetAPI(properties);
    }

    private String joinPropertyNames(final List<Property> properties) {
        final Function<Property, String> getName = new Function<Property, String>() {
            @Override
            public String apply(final Property input) {
                return input.getName();
            }
        };

        return FluentIterable.from(properties)
                .transform(getName)
                .join(Joiner.on(","));
    }

    private List<Property> getMissingProperties(final Properties properties) {
        final List<Property> result = new ArrayList<>();


        final Description description = getDescription();
        for (final Property property : description.getProperties()) {
            if (!property.isRequired()) {
                continue;
            }

            final String key = property.getName();
            final String value = properties.getProperty(key, "");

            final boolean isMissing = isBlank(value);
            if (isMissing) {
                result.add(property);
            }

        }

        return result;
    }

    public Map<String, Object> getMapping(final Properties properties) {
        final Type mappingType = new TypeToken<Map<String, Object>>() {}.getType();
        final String mappingFilePath = properties.getProperty(PROPERTY_MAPPING_FILE);

        if (isNotBlank(mappingFilePath)) {
            final File mappingFile = new File(mappingFilePath);
            final boolean validFile = mappingFile.exists() && mappingFile.canRead();

            if (validFile) {
                final String mappingFileContents = readFromFile(mappingFile);
                try {
                    return gson.fromJson(mappingFileContents, mappingType);
                } catch (Exception ex) {
                    log.warn("while reading mapping file: " + mappingFilePath, ex);
                }
            } else {
                log.warn("can't access mapping file : " + mappingFilePath);
            }
        }

        try {
            final String mappingFileContents = readFromClasspath("defaultMapping.json");
            return gson.fromJson(mappingFileContents, mappingType);
        } catch (Exception ex) {
            log.warn("while reading default mapping", ex);
        }

        return emptyMap();
    }

    private String readFromFile(final File file) {
        try (final InputStream inputStream = new FileInputStream(file);
             final Scanner scanner = new Scanner(inputStream)) {
            scanner.useDelimiter("\\Z");
            return scanner.next();
        } catch (Exception ex) {
            log.warn("can't read file: " + file.getAbsolutePath(), ex);
        }

        return "";
    }

    private String readFromClasspath(final String name) {
        try (final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(name);
             final Scanner scanner = new Scanner(inputStream)) {
            scanner.useDelimiter("\\Z");
            return scanner.next();
        } catch (Exception ex) {
            log.warn("can't read " + name + " form classpath", ex);
        }

        return "";
    }

}
