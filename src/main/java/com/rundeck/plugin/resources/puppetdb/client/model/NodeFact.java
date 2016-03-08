package com.rundeck.plugin.resources.puppetdb.client.model;

import com.google.common.base.Function;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by greg on 3/7/16.
 */
public class NodeFact extends Fact implements Certname {
    public static final Type LIST = new TypeToken<List<NodeFact>>() {
    }.getType();

    public static Function<String, List<NodeFact>> parser(final Gson gson) {
        return new Function<String, List<NodeFact>>() {
            @Override
            public List<NodeFact> apply(final String input) {
                return gson.fromJson(input, NodeFact.LIST);
            }

        };
    }

    private String certname;

    public String getCertname() {
        return certname;
    }

    public void setCertname(String certname) {
        this.certname = certname;
    }
}
