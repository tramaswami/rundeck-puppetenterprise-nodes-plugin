package com.rundeck.plugin.resources.puppetdb;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.*;

import com.rundeck.plugin.resources.puppetdb.client.PuppetDB;
import com.rundeck.plugin.resources.puppetdb.client.model.*;
import org.junit.Test;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.rundeck.plugin.resources.puppetdb.client.PuppetAPI;

public class UT_Mapper_nested_null {
    private final String resourcePath = "nested_null";

    private final Mapper mapper = new Mapper(Optional.<String>absent());

    private final PuppetAPI testApi = TestUtilities.getPuppetApiStub(resourcePath);

    private Map<String, Object> mapping;

    @Test
    public void missing_nested_property_should_be_parsed_as_empty() {
        // this mapping has a invalid mapping for one of the attributes
        PuppetDB db = new PuppetDB(testApi);
        this.mapping = TestUtilities.getMapping(resourcePath + "/nested_null_mapping.json");
        final List<Node> nodes = testApi.getNodes(null);

        final List<PuppetDBNode> nodesWithFacts = FluentIterable.from(nodes)
            .transform(db.queryNode("Class"))
            .toList();

        for (int i = 0; i < nodesWithFacts.size(); i++) {
            final PuppetDBNode puppetDBNode = nodesWithFacts.get(i);

            final Optional<INodeEntry> maybeNode = mapper.apply(puppetDBNode, mapping);
            assertTrue(format("maybeNode[%d] should be present", i), maybeNode.isPresent());

            final INodeEntry node = maybeNode.get();
            // "nestedNull" was set to trigger the exception. to check, just remove it from catch clause at Mapper.java 
            final String nestedNull = node.getAttributes().get("nestedNull");
            assertEquals("nestedNull property should be empty", "", nestedNull);
        }
    }

}
