package com.rundeck.plugin.resources.puppetdb;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException;
import org.junit.Test;

public class ResourceModelFactory_UT implements Constants {

    @Test
    public void test_required_properties() throws ConfigurationException {
        final Properties properties = new Properties();
        properties.put(PROPERTY_PUPPETDB_SSL_DIR, "ssl");
        properties.put(PROPERTY_PUPPETDB_HOST, "localhost");
        properties.put(PROPERTY_PUPPETDB_PORT, "8081");

        // web api
        final ResourceModelFactory resourceModelFactory = new ResourceModelFactory(null);
        resourceModelFactory.getPuppetAPI(properties);
    }

    @Test(expected = ConfigurationException.class)
    public void test_missing_properties() throws ConfigurationException {
        final Properties properties = new Properties();
        properties.put(PROPERTY_PUPPETDB_HOST, "localhost");

        final ResourceModelFactory resourceModelFactory = new ResourceModelFactory(null);
        resourceModelFactory.getPuppetAPI(properties);
    }

    public void test2() throws IOException{
        final InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("defaultMapping.properties");
        final Properties defaultMapping = new Properties();

        if (null != resourceAsStream) {
            try {
                defaultMapping.load(resourceAsStream);
            } finally {
                resourceAsStream.close();
            }

        }

        System.out.println(defaultMapping);
    }




}
