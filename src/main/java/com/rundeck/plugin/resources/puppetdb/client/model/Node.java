package com.rundeck.plugin.resources.puppetdb.client.model;

import java.lang.reflect.Type;
import java.util.List;

import com.google.common.base.Function;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Node implements Certname {

    public static final Type LIST = new TypeToken<List<Node>>() {}.getType();
    public static Function<String, List<Node>> listParser(final Gson gson) {
        return new Function<String, List<Node>>() {
            @Override
            public List<Node> apply(final String input) {
                return gson.fromJson(input, Node.LIST);
            }

        };
    }

    private String certname;

    public String getCertname() {
        return certname;
    }

    public void setCertname(final String certname) {
        this.certname = certname;
    }

}

