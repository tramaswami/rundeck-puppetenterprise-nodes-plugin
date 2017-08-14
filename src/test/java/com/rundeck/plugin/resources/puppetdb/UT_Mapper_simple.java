package com.rundeck.plugin.resources.puppetdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.*;
import java.util.Map.Entry;

import com.rundeck.plugin.resources.puppetdb.client.PuppetDB;
import com.rundeck.plugin.resources.puppetdb.client.model.*;
import org.junit.Test;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.rundeck.plugin.resources.puppetdb.client.PuppetAPI;

public class UT_Mapper_simple {
    private final String resourcePath = "simple";

    private final Mapper mapper = new Mapper(Optional.<String>absent());

    private final PuppetAPI testApi = TestUtilities.getPuppetApiStub(resourcePath);
    private final PuppetDB db = new PuppetDB(testApi);
    private final ImmutableList<PuppetDBNode> nodesWithFacts = FluentIterable.from(testApi.getNodes(null))
            .transform(db.queryNode("Class"))
            .toList();

    private Map<String, Object> mapping;

    @Test
    public void test_known_mapping() {
        this.mapping = TestUtilities.getMapping(resourcePath + "/known_mapping.json");

        final PuppetDBNode puppetDBNode = nodesWithFacts.get(0);

        final Optional<INodeEntry> maybeNode = mapper.apply(puppetDBNode, mapping);
        final INodeEntry nodeEntry = maybeNode.orNull();

        assertTrue("maybeNode should be present", maybeNode.isPresent());
        assertEquals("nodeEntry.hostname should be 100.112.162.79", "100.112.162.79", nodeEntry.getHostname());
        assertEquals("nodeEntry.username should be username", "username", nodeEntry.getUsername());
    }

    @Test
    public void test_known_mapping_with_missing_property() {
        this.mapping = TestUtilities.getMapping(resourcePath + "/known_mapping_with_missing_property.json");

        final PuppetDBNode puppetDBNode = nodesWithFacts.get(0);

        final Optional<INodeEntry> maybeNode = mapper.apply(puppetDBNode, mapping);
        final INodeEntry nodeEntry = maybeNode.orNull();

        assertTrue("maybeNode should be present", maybeNode.isPresent());
        assertEquals("nodeEntry.hostname should be 100.112.162.79", "100.112.162.79", nodeEntry.getHostname());
        assertEquals("nodeEntry.username should be username", "username", nodeEntry.getUsername());
        for (Entry<String, String> e : nodeEntry.getAttributes().entrySet()) {
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
