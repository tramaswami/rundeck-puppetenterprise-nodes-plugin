package com.rundeck.plugin.resources.puppetdb.client.model;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;

public class Node implements Certname {

    public static final Type LIST = new TypeToken<List<Node>>() {}.getType();

    private String certname;

    public String getCertname() {
        return certname;
    }

    public void setCertname(final String certname) {
        this.certname = certname;
    }

}

