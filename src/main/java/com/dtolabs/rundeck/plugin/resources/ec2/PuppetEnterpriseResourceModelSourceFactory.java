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
package com.dtolabs.rundeck.plugin.resources.ec2;

import com.dtolabs.rundeck.core.common.Framework;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.*;
import com.dtolabs.rundeck.core.resources.ResourceModelSource;
import com.dtolabs.rundeck.core.resources.ResourceModelSourceFactory;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;

import java.util.*;

/**
 * PuppetEnterpriseResourceModelSourceFactory is the factory that can create a {@link ResourceModelSource} based on a configuration.
 * <p/>
 */
@Plugin(name = "aws-ec2", service = "ResourceModelSource")
public class PuppetEnterpriseResourceModelSourceFactory implements ResourceModelSourceFactory, Describable {
    public static final String PROVIDER_NAME = "puppet-enterprise";
    private Framework framework;

    public static final String SSL_FOLDER = "sslFolder";
    public static final String HTTPS_HOST = "httpsHost";
    public static final String HTTPS_PORT = "httpsPort";
    //public static final String FILTER_PARAMS = "filter";
    //public static final String MAPPING_PARAMS = "mappingParams";
    //public static final String RUNNING_ONLY = "runningOnly";
    //public static final String MAPPING_FILE = "mappingFile";
    //public static final String REFRESH_INTERVAL = "refreshInterval";
    //public static final String USE_DEFAULT_MAPPING = "useDefaultMapping";
    //public static final String HTTP_PROXY_HOST = "httpProxyHost";
    //public static final String HTTP_PROXY_PORT = "httpProxyPort";

    public PuppetEnterpriseResourceModelSourceFactory(final Framework framework) {
        this.framework = framework;
    }

    public ResourceModelSource createResourceModelSource(final Properties properties) throws ConfigurationException {
        final PuppetEnterpriseResourceModelSource puppetEnterpriseResourceModelSource = new PuppetEnterpriseResourceModelSource(properties);
        puppetEnterpriseResourceModelSource.validate();
        return puppetEnterpriseResourceModelSource;
    }

    static Description DESC = DescriptionBuilder.builder()
            .name(PROVIDER_NAME)
            .title("Puppet Enterprise Resources")
            .description("Produces nodes from Puppet Enterprise")

            .property(PropertyUtil.string(SSL_FOLDER, "SSL Folder", "AWS EC2 filters", false, null))
            .property(PropertyUtil.string(HTTPS_HOST, "Puppet Enterprise HTTPS Host", "API hostname", false, null))
            .property(PropertyUtil.integer(HTTPS_PORT, "Puppet Enterprise HTTPS Port", "API port or blank for 8081", false, "8081"))
            /*.property(PropertyUtil.integer(REFRESH_INTERVAL, "Refresh Interval",
                    "Minimum time in seconds between API requests to AWS (default is 30)", false, "30"))
            .property(PropertyUtil.string(FILTER_PARAMS, "Filter Params", "AWS EC2 filters", false, null))
            .property(PropertyUtil.string(ENDPOINT, "Endpoint", "AWS EC2 Endpoint, or blank for default", false, null))
            .property(PropertyUtil.string(HTTP_PROXY_HOST, "HTTP Proxy Host", "HTTP Proxy Host Name, or blank for default", false, null))
            .property(PropertyUtil.integer(HTTP_PROXY_PORT, "HTTP Proxy Port", "HTTP Proxy Port, or blank for 80", false, "80"))
            .property(PropertyUtil.string(HTTP_PROXY_USER, "HTTP Proxy User", "HTTP Proxy User Name, or blank for default", false, null))
            .property(
                    PropertyUtil.string(
                            HTTP_PROXY_PASS,
                            "HTTP Proxy Password",
                            "HTTP Proxy Password, or blank for default",
                            false,
                            null,
                            null,
                            null,
                            Collections.singletonMap("displayType", (Object) StringRenderingConstants.DisplayType.PASSWORD)
                    )
            )
            .property(PropertyUtil.string(MAPPING_PARAMS, "Mapping Params",
                    "Property mapping definitions. Specify multiple mappings in the form " +
                            "\"attributeName.selector=selector\" or \"attributeName.default=value\", " +
                            "separated by \";\"",
                    false, null))
            .property(PropertyUtil.string(MAPPING_FILE, "Mapping File", "Property mapping File", false, null,
                    new PropertyValidator() {
                        public boolean isValid(final String s) throws ValidationException {
                            if (!new File(s).isFile()) {
                                throw new ValidationException("File does not exist: " + s);
                            }
                            return true;
                        }
                    }))
            .property(PropertyUtil.bool(USE_DEFAULT_MAPPING, "Use Default Mapping",
                    "Start with default mapping definition. (Defaults will automatically be used if no others are " +
                            "defined.)",
                    false, "true"))
            .property(PropertyUtil.bool(RUNNING_ONLY, "Only Running Instances",
                    "Include Running state instances only. If false, all instances will be returned that match your " +
                            "filters.",
                    false, "true"))
*/
            .build();

    public Description getDescription() {
        return DESC;
    }
}
