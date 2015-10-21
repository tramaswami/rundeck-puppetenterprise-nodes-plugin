package com.rundeck.plugin.resources.puppetdb.client.model;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.LinkedHashMap;
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
        this.facts = facts.stream().collect(toMap(Fact::getName, Fact::getValue));
        this.classes = nodeClasses.stream().map(NodeClass::getTitle).collect(toList());
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
