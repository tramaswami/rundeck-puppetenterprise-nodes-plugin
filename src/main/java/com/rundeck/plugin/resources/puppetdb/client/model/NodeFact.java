package com.rundeck.plugin.resources.puppetdb.client.model;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by greg on 3/7/16.
 */
public class NodeFact extends Fact {
    public static final Type LIST = new TypeToken<List<NodeFact>>() {
    }.getType();

    private String certname;

    public String getCertname() {
        return certname;
    }

    public void setCertname(String certname) {
        this.certname = certname;
    }
}
