package com.rundeck.plugin.resources.puppetdb.client;

import java.util.List;

import com.google.common.base.Function;
import com.rundeck.plugin.resources.puppetdb.client.model.Fact;
import com.rundeck.plugin.resources.puppetdb.client.model.Node;
import com.rundeck.plugin.resources.puppetdb.client.model.NodeClass;
import com.rundeck.plugin.resources.puppetdb.client.model.PuppetDBNode;

public abstract class PuppetAPI {

    public abstract List<Node> getNodes();

    public abstract List<Fact> getFactsForNode(final Node node);

    public abstract List<NodeClass> getClassesForNode(final Node node);

    public PuppetDBNode getNodeWithFacts(final Node node) {
        final List<Fact> facts = getFactsForNode(node);
        final List<NodeClass> nodeClasses = getClassesForNode(node);
        return new PuppetDBNode(node, facts, nodeClasses);
    }

    public Function<Node, PuppetDBNode> queryNode() {
        return new Function<Node, PuppetDBNode>() {
            @Override
            public PuppetDBNode apply(final Node node) {
                return getNodeWithFacts(node);
            }
        };
    }

}
