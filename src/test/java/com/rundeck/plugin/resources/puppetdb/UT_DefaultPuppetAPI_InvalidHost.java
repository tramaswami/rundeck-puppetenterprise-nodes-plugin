package com.rundeck.plugin.resources.puppetdb;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import com.codahale.metrics.MetricRegistry;
import com.rundeck.plugin.resources.puppetdb.client.DefaultPuppetAPI;
import com.rundeck.plugin.resources.puppetdb.client.model.Node;
import org.junit.Test;

public class UT_DefaultPuppetAPI_InvalidHost implements Constants {

    @Test
    public void invalid_host() {
        final Properties properties = new Properties();

        properties.put(PROPERTY_PUPPETDB_HOST, "non-existing-host");
        properties.put(PROPERTY_PUPPETDB_PORT, "1234");

        final DefaultPuppetAPI defaultPuppetAPI = new DefaultPuppetAPI(properties, new MetricRegistry());
        final List<Node> nodes = defaultPuppetAPI.getNodes();

        assertNotNull("nodes should not be null", nodes);
        assertTrue("nodes should be empty", nodes.isEmpty());
    }

}
