package com.rundeck.plugin.resources.puppetdb.client;

import java.util.List;

import com.rundeck.plugin.resources.puppetdb.client.model.Fact;
import com.rundeck.plugin.resources.puppetdb.client.model.Node;
import com.rundeck.plugin.resources.puppetdb.client.model.NodeClass;
import com.rundeck.plugin.resources.puppetdb.client.model.PuppetDBNode;

public interface PuppetAPI {

    List<Node> getNodes();

    List<Fact> getFactsForNode(final Node node);

    List<NodeClass> getClassesForNode(final Node node);

    default PuppetDBNode getNodeWithFacts(final Node node) {
        final List<Fact> facts = getFactsForNode(node);
        final List<NodeClass> nodeClasses = getClassesForNode(node);
        return new PuppetDBNode(node, facts, nodeClasses);
    }

}
