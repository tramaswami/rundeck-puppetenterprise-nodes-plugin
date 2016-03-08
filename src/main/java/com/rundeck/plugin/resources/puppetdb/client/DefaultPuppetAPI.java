package com.rundeck.plugin.resources.puppetdb.client;

import static java.lang.String.format;

import java.util.*;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.rundeck.plugin.resources.puppetdb.Constants;
import com.rundeck.plugin.resources.puppetdb.client.model.*;
import org.apache.log4j.Logger;

public class DefaultPuppetAPI extends PuppetAPI implements Constants {


    private static final Logger LOG = Logger.getLogger(DefaultPuppetAPI.class);


    private final String puppetNodeQuery;
    private final HTTP http;
    private static final Gson GSON = new Gson();

    public DefaultPuppetAPI(final HTTP http, final String puppetNodeQuery) {
        this.puppetNodeQuery = puppetNodeQuery;
        this.http = http;
    }


    @Override
    public List<Node> getNodes() {
        final String path = mkQuery("pdb/query/v4/nodes", getUserQuery());
        return http.makeRequest(path, Node.listParser(GSON), Collections.<Node>emptyList(), "getNodes()");
    }

    Function<String, List<NodeFact>> singleFact() {
        return new Function<String, List<NodeFact>>() {
            @Override
            public List<NodeFact> apply(final String input) {
                return getFactForAllNodes(input);
            }
        };
    }

    public List<NodeFact> getFactSet(Set<String> facts) {
        ImmutableList<List<NodeFact>> lists = FluentIterable.from(facts).transform(singleFact()).toList();
        List<NodeFact> nodeFacts = new ArrayList<>();
        for (List<NodeFact> list : lists) {
            nodeFacts.addAll(list);
        }
        return nodeFacts;
    }

    public List<NodeFact> getFactForAllNodes(String fact) {
        final String path = mkQuery(format("pdb/query/v4/facts/%s", fact), getUserQuery());
        return http.makeRequest(path, NodeFact.parser(GSON), Collections.<NodeFact>emptyList(), "getFactForAllNodes()");
    }

    private String getUserQuery() {
        return (puppetNodeQuery != null && !puppetNodeQuery.trim().isEmpty())
               ? ("?query=") + puppetNodeQuery
               : "";
    }


    @Override
    public List<NodeClass> getClassesForNode(final Node node) {
        final String path = format("pdb/query/v4/nodes/%s/resources/Class", node.getCertname());

        return http.makeRequest(
                path,
                NodeClass.parser(GSON),
                Collections.<NodeClass>emptyList(),
                "getClassesForNode()"
        );
    }

    public List<CertNodeClass> getClassesForAllNodes() {
        final String path = mkQuery("pdb/query/v4/resources/Class", getUserQuery());
        return http.makeRequest(
                path,
                CertNodeClass.listParser(GSON), Collections.<CertNodeClass>emptyList(), "getClassesForAllNodes()"
        );
    }

    private String mkQuery(final String path, String query) {
        return path + (query != null ? query : "");
    }

    @Override
    public List<Fact> getFactsForNode(final Node node) {
        final String url = format("pdb/query/v4/nodes/%s/facts", node.getCertname());
        return http.makeRequest(url, Fact.listParser(GSON), Collections.<Fact>emptyList(), "getFactsForNode()");
    }


}
