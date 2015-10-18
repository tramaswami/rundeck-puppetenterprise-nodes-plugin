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
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;
import java.util.Properties;

import com.dtolabs.rundeck.core.common.Framework;
import com.dtolabs.rundeck.core.common.NodeSetImpl;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.core.plugins.configuration.Property;
import com.dtolabs.rundeck.core.resources.ResourceModelSource;
import com.dtolabs.rundeck.core.resources.ResourceModelSourceFactory;
import com.rundeck.plugin.resources.puppetdb.client.DefaultPuppetAPI;
import com.rundeck.plugin.resources.puppetdb.client.PuppetAPI;
import org.apache.log4j.Logger;

@Plugin(name = "puppet-enterprise", service = "ResourceModelSource")
public class ResourceModelFactory implements ResourceModelSourceFactory, Describable, Constants {

    private static Logger log = Logger.getLogger(ResourceModelFactory.class);

    private Framework framework;

    public ResourceModelFactory(final Framework framework) {
        this.framework = framework;
    }

    @Override
    public ResourceModelSource createResourceModelSource(final Properties properties) throws ConfigurationException {

        // TODO: mapping

        return () -> new NodeSetImpl();
    }

    @Override
    public Description getDescription() {
        return PLUGIN_DESCRIPTION;
    }

    public PuppetAPI getPuppetAPI(final Properties properties) throws ConfigurationException {
        final List<Property> missingProperties = getMissingProperties(properties);
        if (isNotEmpty(missingProperties)) {
            final String missingPropertiesNames = missingProperties
                    .stream()
                    .map(Property::getName)
                    .collect(joining(","));


            final String template = "Can't start %s plugin, Missing properties: '%s'";
            final String message = format(template, PROVIDER_NAME, missingPropertiesNames);
            throw new ConfigurationException(message);
        }


        return new DefaultPuppetAPI(properties);
    }

    private List<Property> getMissingProperties(final Properties properties) {
        return getDescription()
                .getProperties()
                .stream()
                .filter(Property::isRequired)
                .filter(property -> {
                    final String key = property.getName();
                    final String value = properties.getProperty(key);
                    return isBlank(value);
                })
                .collect(toList());

    }

}
