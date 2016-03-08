package com.rundeck.plugin.resources.puppetdb;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rundeck.plugin.resources.puppetdb.client.PuppetAPI;
import com.rundeck.plugin.resources.puppetdb.client.model.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by greg on 3/7/16.
 */
public class TestDetermineFactNames {


    Mapper mapper;

    Gson gson;
    PuppetAPI testApi;
    Map<String, Object> mapping;

    @Before
    public void before() {
        this.mapper = new Mapper(new Properties());
        this.gson = new Gson();
        this.testApi = testApi();
    }


    @Test
    public void test_known_mapping() {
        this.mapping = getMapping("simple/known_mapping.json");
        Set<String> strings = mapper.determineFactNames(mapping);
        Object[] a = strings.toArray();
        Arrays.sort(a);
        Assert.assertArrayEquals(
                new String[]{"architecture","hardwareisa", "ipaddress", "kernelversion","os"},
                a
        );
    }

    public Map<String, Object> getMapping(final String fileName) {
        final Type mappingType = new TypeToken<Map<String, Object>>() {
        }.getType();
        return gson.fromJson(readFile(fileName), mappingType);
    }

    public PuppetAPI testApi() {
        return new PuppetAPI() {
            @Override
            public List<CertNodeClass> getClassesForAllNodes() {
                return null;
            }

            @Override
            public List<NodeFact> getFactSet(final Set<String> facts) {
                return null;
            }

            @Override
            public List<Node> getNodes() {
                return gson.fromJson(readFile("simple/nodes.json"), Node.LIST);
            }

            @Override
            public List<Fact> getFactsForNode(final Node node) {
                return gson.fromJson(readFile("simple/facts.json"), Fact.LIST);
            }

            @Override
            public List<NodeClass> getClassesForNode(final Node node) {
                return gson.fromJson(readFile("simple/classes.json"), NodeClass.LIST);
            }
        };
    }

    public String readFile(final String name) {
        try (
                final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(name);
                final Scanner scanner = new Scanner(inputStream)
        ) {
            scanner.useDelimiter("\\Z");
            return scanner.next();
        } catch (Exception ex) {
            System.err.println("can't read file: " + name);
            ex.printStackTrace(System.err);
        }

        return "";
    }

}
