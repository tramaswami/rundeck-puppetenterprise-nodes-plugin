package com.rundeck.plugin.resources.puppetdb;

import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyUtil;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;

public interface Constants {

    String PROVIDER_NAME = "puppet-enterprise";

    String PROPERTY_PUPPETDB_HOST = "puppetdb_host";
    String PROPERTY_PUPPETDB_PORT = "puppetdb_port";
    String PROPERTY_PUPPETDB_SSL_DIR = "puppetdb_ssl_dir";
    String PROPERTY_MAPPING_FILE = "mappingFile";

    Description PLUGIN_DESCRIPTION = DescriptionBuilder.builder()
            .name(PROVIDER_NAME)
            .title("PuppetDB Nodes Plugin")
            .description("Produces Nodes from PuppetDB")
            .property(PropertyUtil.string(PROPERTY_PUPPETDB_HOST, "PuppetDB Host", "Puppet DB hostname", true, null))
            .property(PropertyUtil.integer(PROPERTY_PUPPETDB_PORT, "PuppetDB Port", "Puppet DB port (defaults to 8001)", true, "8081"))
            .property(PropertyUtil.integer(PROPERTY_PUPPETDB_SSL_DIR, "PuppetDB SSL Directory", "local directory for SSL, if null it'll use http, "
                    + "it shoud contain <ssl directory>/private_keys/<puppetdb host>.pem "
                    + "and <ssl directory>/certs/<puppetdb host>.pem "
                    + "and <ssl directory>/ca/ca_crt.pem", true, null))
            .property(PropertyUtil.string(PROPERTY_MAPPING_FILE, "Property Mapping JSON", "JSON File describing mapping between PuppetDB Data and Rundeck Data", false, ""))
            .build();

}
