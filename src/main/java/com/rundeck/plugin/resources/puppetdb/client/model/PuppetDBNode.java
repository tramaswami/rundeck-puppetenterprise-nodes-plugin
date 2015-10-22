package com.rundeck.plugin.resources.puppetdb.client.model;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class PuppetDBNode {

    private final Node node;
    private final Map<String, Object> facts;
    private final List<String> classes;

    public PuppetDBNode(final Node node,
                        final List<Fact> facts,
                        final List<NodeClass> nodeClasses) {
        this.node = node;

        this.facts = new LinkedHashMap<>();
        for (final Fact fact : facts) {
            this.facts.put(fact.getName(), fact.getValue());
        }

        this.classes = new LinkedList<>();
        for (final NodeClass nodeClass : nodeClasses) {
            this.classes.add(nodeClass.getTitle());
        }
    }

    public String getCertname() {
        return node.getCertname();
    }

    public Map<String, Object> getFacts() {
        return new LinkedHashMap<>(facts);
    }

    public List<String> getClasses() {
        return classes;
    }

    @Override
    public String toString() {
        return "PuppetDBNode{" +
                "classes=" + classes +
                ", node=" + node +
                ", facts=" + facts +
                '}';
    }
}
