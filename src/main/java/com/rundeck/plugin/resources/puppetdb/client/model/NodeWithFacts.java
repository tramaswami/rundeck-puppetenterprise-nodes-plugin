package com.rundeck.plugin.resources.puppetdb.client.model;

import static java.util.stream.Collectors.toMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class NodeWithFacts {

    private final Node node;
    private final Map<String, Object> facts;

    public NodeWithFacts(final Node node, final List<Fact> facts) {
        this.node = node;
        this.facts = facts.stream().collect(toMap(Fact::getName, Fact::getValue));
    }

    public String getCertname() {
        return node.getCertname();
    }

    public Map<String, Object> getFacts() {
        return new LinkedHashMap<>(facts);
    }

    @Override
    public String toString() {
        return "NodeWithFacts{" +
                "facts=" + facts +
                ", node=" + node +
                '}';
    }
}
