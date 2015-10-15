package com.rundeck.plugin.resources.puppetdb.client.model;

import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;

public class NodeWithFacts {

    private final Node node;
    private final Map<String, String> facts;

    public NodeWithFacts(final Node node, final List<Fact> facts) {
        this.node = node;
        this.facts = facts.stream().collect(toMap(Fact::getName, fact -> fact.getValue().toString()));
    }

    @Override
    public String toString() {
        return "NodeWithFacts{" +
                "facts=" + facts +
                ", node=" + node +
                '}';
    }
}
