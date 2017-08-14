package com.rundeck.plugin.resources.puppetdb;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Map;

import com.dtolabs.rundeck.core.common.INodeEntry;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import com.rundeck.plugin.resources.puppetdb.client.PuppetAPI;
import com.rundeck.plugin.resources.puppetdb.client.PuppetDB;
import com.rundeck.plugin.resources.puppetdb.client.model.PuppetDBNode;

public class TestConditionalAttributes {
    private final String resourcePath = "conditional_attributes";

    private final Mapper mapper = new Mapper(Optional.<String>absent());

    private final PuppetAPI testApi = TestUtilities.getPuppetApiStub(resourcePath);
    private final PuppetDB db = new PuppetDB(testApi);
    private final ImmutableList<PuppetDBNode> nodesWithFacts = FluentIterable.from(testApi.getNodes(null))
                                                                             .transform(db.queryNode("Class"))
                                                                             .toList();

    private Map<String, Object> mapping;

    private final String linuxCertName = "localhost-linux";
    private final String windowsCertName = "localhost-windows";
    private final String firstConditionalAttributeName = "firstConditionalAttribute";
    private final String firstConditionalAttributeValue = "test";

    @Test
    public void testApply_regexMatchesOneNodeAndDefaultValue_conditionalAttributeAddedToMatchedNodeOnly() {
        this.mapping = TestUtilities.getMapping(resourcePath + "/one_matching_mapping.json");

        final PuppetDBNode linuxNode = nodesWithFacts.get(0);
        assertEquals("linux test node should be present", linuxCertName, linuxNode.getCertname());
        final PuppetDBNode windowsNode = nodesWithFacts.get(1);
        assertEquals("windows test node should be present", windowsCertName, windowsNode.getCertname());

        final Optional<INodeEntry> linuxEntry = mapper.apply(linuxNode, mapping);
        assertTrue("linux node apply result should be present", linuxEntry.isPresent());

        assertFalse("linux node should not contain conditional attribute", linuxEntry.get().getAttributes().containsKey(firstConditionalAttributeName));

        final Optional<INodeEntry> windowsEntry = mapper.apply(windowsNode, mapping);
        assertTrue("windows node apply result should be present", windowsEntry.isPresent());

        assertTrue("windows node should contain conditional attribute",
                windowsEntry.get().getAttributes().containsKey(firstConditionalAttributeName));
        assertEquals("conditional attribute should equal default value",
                firstConditionalAttributeValue, windowsEntry.get().getAttributes().get(firstConditionalAttributeName));
    }

    @Test
    public void testApply_regexDoesNotMatchAnyNodes_conditionalAttributeNotAddedToNodes() {
        this.mapping = TestUtilities.getMapping(resourcePath + "/no_matching_mappings.json");

        final PuppetDBNode linuxNode = nodesWithFacts.get(0);
        assertEquals("linux test node should be present", linuxCertName, linuxNode.getCertname());
        final PuppetDBNode windowsNode = nodesWithFacts.get(1);
        assertEquals("windows test node should be present", windowsCertName, windowsNode.getCertname());

        final Optional<INodeEntry> linuxEntry = mapper.apply(linuxNode, mapping);
        assertTrue("linux node apply result should be present", linuxEntry.isPresent());

        assertFalse("linux node should not contain conditional attribute", linuxEntry.get().getAttributes().containsKey(firstConditionalAttributeName));

        final Optional<INodeEntry> windowsEntry = mapper.apply(windowsNode, mapping);
        assertTrue("windows node apply result should be present", windowsEntry.isPresent());

        assertFalse("windows node should not contain conditional attribute", windowsEntry.get().getAttributes().containsKey(firstConditionalAttributeName));
    }

    @Test
    public void testApply_regexMatchesTwoNodesAndDefaultValue_conditionalAttributeAddedToBothNodes() {
        this.mapping = TestUtilities.getMapping(resourcePath + "/two_matching_mappings.json");

        final PuppetDBNode linuxNode = nodesWithFacts.get(0);
        assertEquals("linux test node should be present", linuxCertName, linuxNode.getCertname());
        final PuppetDBNode windowsNode = nodesWithFacts.get(1);
        assertEquals("windows test node should be present", windowsCertName, windowsNode.getCertname());

        final Optional<INodeEntry> linuxEntry = mapper.apply(linuxNode, mapping);
        assertTrue("linux node apply result should be present", linuxEntry.isPresent());

        assertTrue("linux node should contain conditional attribute",
                linuxEntry.get().getAttributes().containsKey(firstConditionalAttributeName));
        assertEquals("conditional attribute should equal default value",
                firstConditionalAttributeValue, linuxEntry.get().getAttributes().get(firstConditionalAttributeName));

        final Optional<INodeEntry> windowsEntry = mapper.apply(windowsNode, mapping);
        assertTrue("windows node apply result should be present", windowsEntry.isPresent());

        assertTrue("windows node should contain conditional attribute",
                windowsEntry.get().getAttributes().containsKey(firstConditionalAttributeName));
        assertEquals("conditional attribute should equal default value",
                firstConditionalAttributeValue, windowsEntry.get().getAttributes().get(firstConditionalAttributeName));
    }

    @Test
    public void testApply_conditionPathNotFound_conditionalAttributeNotFound() {
        this.mapping = TestUtilities.getMapping(resourcePath + "/condition_path_not_found_mapping.json");

        final PuppetDBNode windowsNode = nodesWithFacts.get(1);
        assertEquals("windows test node should be present", windowsCertName, windowsNode.getCertname());

        final Optional<INodeEntry> windowsEntry = mapper.apply(windowsNode, mapping);
        assertTrue("windows node apply result should be present", windowsEntry.isPresent());

        assertFalse("windows node should not contain conditional attribute", windowsEntry.get().getAttributes().containsKey(firstConditionalAttributeName));
    }

    @Test
    public void testApply_conditionNotComplete_conditionalAttributeNotFound() {
        this.mapping = TestUtilities.getMapping(resourcePath + "/condition_path_not_found_mapping.json");

        final PuppetDBNode windowsNode = nodesWithFacts.get(1);
        assertEquals("windows test node should be present", windowsCertName, windowsNode.getCertname());

        final Optional<INodeEntry> windowsEntry = mapper.apply(windowsNode, mapping);
        assertTrue("windows node apply result should be present", windowsEntry.isPresent());

        assertFalse("windows node should not contain conditional attribute", windowsEntry.get().getAttributes().containsKey(firstConditionalAttributeName));
    }

    @Test
    public void testApply_conditionInvalidRegex_conditionalAttributeNotFound() {
        this.mapping = TestUtilities.getMapping(resourcePath + "/invalid_regex_mapping.json");

        final PuppetDBNode windowsNode = nodesWithFacts.get(1);
        assertEquals("windows test node should be present", windowsCertName, windowsNode.getCertname());

        final Optional<INodeEntry> windowsEntry = mapper.apply(windowsNode, mapping);
        assertTrue("windows node apply result should be present", windowsEntry.isPresent());

        assertFalse("windows node should not contain conditional attribute", windowsEntry.get().getAttributes().containsKey(firstConditionalAttributeName));
    }


}
