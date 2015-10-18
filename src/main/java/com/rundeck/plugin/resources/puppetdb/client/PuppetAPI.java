package com.rundeck.plugin.resources.puppetdb.client;

import java.util.List;

import com.rundeck.plugin.resources.puppetdb.client.model.Fact;
import com.rundeck.plugin.resources.puppetdb.client.model.Node;
import com.rundeck.plugin.resources.puppetdb.client.model.NodeWithFacts;

public interface PuppetAPI {

    List<Node> getNodes();

    List<Fact> getFactsForNode(final Node node);

    default NodeWithFacts getNodeWithFacts(final Node node) {
        final List<Fact> facts = getFactsForNode(node);
        return new NodeWithFacts(node, facts);
    }

}
