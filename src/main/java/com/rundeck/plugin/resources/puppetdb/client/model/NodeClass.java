package com.rundeck.plugin.resources.puppetdb.client.model;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;

public class NodeClass {

    public static final Type LIST = new TypeToken<List<NodeClass>>() {}.getType();

    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "NodeClass{" +
                "title='" + title + '\'' +
                '}';
    }

}

