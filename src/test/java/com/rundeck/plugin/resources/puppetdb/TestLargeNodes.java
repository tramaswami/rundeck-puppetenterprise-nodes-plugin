package com.rundeck.plugin.resources.puppetdb;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.rundeck.plugin.resources.puppetdb.client.PuppetAPI;
import com.rundeck.plugin.resources.puppetdb.client.PuppetDB;
import com.rundeck.plugin.resources.puppetdb.client.model.*;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by greg on 3/7/16.
 */
public class TestLargeNodes {
    private final String resourcePath = "simple";

    private final Mapper mapper = new Mapper(Optional.<String>absent());

    private final PuppetAPI testApi = TestUtilities.getPuppetApiStub(resourcePath);

    private Map<String, Object> mapping;

    @Test
    public void test_known_mapping() {
        PuppetDB db = new PuppetDB(testApi);
        this.mapping = TestUtilities.getMapping(resourcePath + "/known_mapping.json");
        final ImmutableList<PuppetDBNode> nodesWithFacts = FluentIterable.from(testApi.getNodes(null))
                                                                         .transform(db.queryNode("Class"))
                                                                         .toList();

        final PuppetDBNode puppetDBNode = nodesWithFacts.get(0);

        final Optional<INodeEntry> maybeNode = mapper.apply(puppetDBNode, mapping);
        final INodeEntry nodeEntry = maybeNode.orNull();

        assertTrue("maybeNode should be present", maybeNode.isPresent());
        assertEquals("nodeEntry.hostname should be 100.112.162.79", "100.112.162.79", nodeEntry.getHostname());
        assertEquals("nodeEntry.username should be username", "username", nodeEntry.getUsername());
    }

    @Test
    public void test_known_mapping_with_missing_property() {
        PuppetDB db = new PuppetDB(testApi);
        this.mapping = TestUtilities.getMapping(resourcePath + "/known_mapping_with_missing_property.json");
        long now = System.currentTimeMillis();
        final ImmutableList<PuppetDBNode> nodesWithFacts = FluentIterable.from(testApi.getNodes(null))
                                                                         .transform(db.queryNode("Class"))
                                                                         .toList();

        System.out.println("dur " + (System.currentTimeMillis() - now));
        final PuppetDBNode puppetDBNode = nodesWithFacts.get(0);

        final Optional<INodeEntry> maybeNode = mapper.apply(puppetDBNode, mapping);
        final INodeEntry nodeEntry = maybeNode.orNull();

        assertTrue("maybeNode should be present", maybeNode.isPresent());
        assertEquals("nodeEntry.hostname should be 100.112.162.79", "100.112.162.79", nodeEntry.getHostname());
        assertEquals("nodeEntry.username should be username", "username", nodeEntry.getUsername());
        for (Map.Entry<String, String> e : nodeEntry.getAttributes().entrySet()) {
            assertTrue(e != null);
            assertTrue(e.getKey() != null);
            assertTrue(e.getValue() != null);
        }
        for (Object o : nodeEntry.getTags()) {
            assertTrue(o != null);
        }

        assertTrue(nodeEntry.getDescription() != null);
        //assertTrue(nodeEntry.getFrameworkProject() != null);
        assertTrue(nodeEntry.getNodename() != null);
        assertTrue(nodeEntry.getOsArch() != null);
        //assertTrue(nodeEntry.getOsFamily() != null);
        assertTrue(nodeEntry.getOsName() != null);
        assertTrue(nodeEntry.getOsVersion() != null);
        assertTrue(nodeEntry.getUsername() != null);
        assertTrue(nodeEntry.extractHostname() != null);
        //assertTrue(nodeEntry.extractPort() != null);
        assertTrue(nodeEntry.extractUserName() != null);
    }
}
