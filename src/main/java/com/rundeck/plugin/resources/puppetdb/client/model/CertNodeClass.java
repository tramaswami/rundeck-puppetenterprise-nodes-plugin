package com.rundeck.plugin.resources.puppetdb.client.model;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by greg on 3/8/16.
 */
public class CertNodeClass extends NodeClass {
    public static final Type LIST = new TypeToken<List<CertNodeClass>>() {}.getType();
    private String certname;

    public String getCertname() {
        return certname;
    }

    public void setCertname(String certname) {
        this.certname = certname;
    }

    @Override
    public String toString() {
        return "com.rundeck.plugin.resources.puppetdb.client.model.CertNodeClass{" +
               "certname='" + certname + '\'' +
               '}';
    }
}
