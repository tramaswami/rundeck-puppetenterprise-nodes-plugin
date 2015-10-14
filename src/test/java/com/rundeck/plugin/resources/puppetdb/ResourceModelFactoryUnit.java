package com.rundeck.plugin.resources.puppetdb;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Properties;

import com.rundeck.plugin.resources.puppetdb.client.DefaultPuppetAPI;
import com.rundeck.plugin.resources.puppetdb.client.model.Fact;
import com.rundeck.plugin.resources.puppetdb.client.model.Node;
import org.junit.Test;

public class ResourceModelFactoryUnit implements Constants {

    @Test
    public void test() {
        final Properties properties = new Properties();
        properties.put(PROPERTY_PUPPETDB_HOST, "localhost");
        properties.put(PROPERTY_PUPPETDB_PORT, "8081");

        // web api
        final DefaultPuppetAPI defaultPuppetAPI = new DefaultPuppetAPI(properties);

        final List<Node> nodes = defaultPuppetAPI.getNodes();

        final List<List<Fact>> collect = nodes.stream().map(node -> defaultPuppetAPI.getFacts(node))
                .collect(toList());

        System.out.println(collect);
    }



}
