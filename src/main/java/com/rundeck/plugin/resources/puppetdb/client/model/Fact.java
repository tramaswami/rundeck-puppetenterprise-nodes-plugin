package com.rundeck.plugin.resources.puppetdb.client.model;

import java.lang.reflect.Type;
import java.util.List;

import com.google.common.base.Function;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Fact {

    public static final Type LIST = new TypeToken<List<Fact>>() {}.getType();

    public static Function<String, List<Fact>> listParser(final Gson gson) {
        return new Function<String, List<Fact>>() {
            @Override
            public List<Fact> apply(final String input) {
                return gson.fromJson(input, Fact.LIST);
            }

        };
    }
    private String environment;
    private String name;
    private Object value;

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(final String environment) {
        this.environment = environment;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Fact{" +
               "environment='" + environment + '\'' +
               ", name='" + name + '\'' +
               ", value=" + value +
               '}';
    }
}
