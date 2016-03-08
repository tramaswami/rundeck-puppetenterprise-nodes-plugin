package com.rundeck.plugin.resources.puppetdb.client;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.rundeck.plugin.resources.puppetdb.client.model.*;

public abstract class PuppetAPI {

    public abstract List<Node> getNodes();

    public abstract List<Fact> getFactsForNode(final Node node);

    public abstract List<NodeClass> getClassesForNode(final Node node);
    public abstract List<NodeFact> getFactSet(Set<String> facts);

    /**
     * Create a puppet DB Node by filtering the facts
     * @param node
     * @param facts
     * @return
     */
    public PuppetDBNode getNodeWithFacts(final Node node, List<NodeFact> facts) {
        final List<NodeFact> newfacts = FluentIterable.from(facts).filter(
                new Predicate<NodeFact>() {
                    @Override
                    public boolean apply(final NodeFact input) {
                        return node.getCertname().equals(input.getCertname());
                    }
                }
        ).toList();

        final List<NodeClass> nodeClasses = Collections.emptyList()
                //getClassesForNode(node)
                ;

        return new PuppetDBNode(node, newfacts, nodeClasses);
    }

    public PuppetDBNode getNodeWithFacts(final Node node) {
        final List<Fact> facts = getFactsForNode(node);
        final List<NodeClass> nodeClasses = getClassesForNode(node);
        return new PuppetDBNode(node, facts, nodeClasses);
    }

    public Function<Node, PuppetDBNode> queryNodeWithFacts(final List<NodeFact> facts) {
        return new Function<Node, PuppetDBNode>() {
            @Override
            public PuppetDBNode apply(final Node node) {
                return getNodeWithFacts(node, facts);
            }
        };
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
