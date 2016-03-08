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
    public abstract List<CertNodeClass> getClassesForAllNodes();
    public abstract List<NodeFact> getFactSet(Set<String> facts);

    /**
     * Create a puppet DB Node by filtering the facts
     * @param node
     * @param facts
     * @param nodeClasses1
     * @return
     */
    public PuppetDBNode getNodeWithFacts(final Node node, final List<NodeFact> facts, final List<CertNodeClass> nodeClasses1) {
        final List<NodeFact> newfacts = FluentIterable.from(facts).filter(certNameEqualsPredicate(node)).toList();
        final List<CertNodeClass> classes = FluentIterable.from(nodeClasses1)
                                                          .filter(certNameEqualsPredicate(node))
                                                          .toList();


        return new PuppetDBNode(node, newfacts, classes);
    }

    private Predicate<Certname> certNameEqualsPredicate(final Certname node) {
        return new Predicate<Certname>() {
            @Override
            public boolean apply(final Certname input) {
                if (input == null ||
                    input.getCertname() == null ||
                    node == null ||
                    node.getCertname() == null) {
                    return false;
                }
                return node.getCertname().equals(input.getCertname());
            }
        };
    }

    public PuppetDBNode getNodeWithFacts(final Node node) {
        final List<Fact> facts = getFactsForNode(node);
        final List<NodeClass> nodeClasses = getClassesForNode(node);
        return new PuppetDBNode(node, facts, nodeClasses);
    }

    public Function<Node, PuppetDBNode> queryNodeWithFacts(final List<NodeFact> facts,final List<CertNodeClass> nodeClasses) {
        return new Function<Node, PuppetDBNode>() {
            @Override
            public PuppetDBNode apply(final Node node) {
                return getNodeWithFacts(node, facts, nodeClasses);
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
