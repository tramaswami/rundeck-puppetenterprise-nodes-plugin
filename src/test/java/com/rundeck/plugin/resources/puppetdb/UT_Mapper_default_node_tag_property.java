package com.rundeck.plugin.resources.puppetdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rundeck.plugin.resources.puppetdb.client.model.PuppetDBNode;
import java.util.ArrayList;
import java.util.HashMap;

public class UT_Mapper_default_node_tag_property implements Constants {

    Gson gson;

    @Before
    public void before() {
        this.gson = new Gson();
    }

    @Test
    public void test_default_tag() {
        final String knownDefaultTag = "knownDefaultTag";

        // object under test, default mapping with no properties
        final Properties properties = new Properties();
        properties.put(PROPERTY_DEFAULT_NODE_TAG, knownDefaultTag);

        final Mapper mapper = new Mapper(properties);

        final Optional<String> defaultTag = mapper.getDefaultNodeTag();

        assertTrue("default tag should be there", defaultTag.isPresent());
        assertEquals("default tag should match", knownDefaultTag, defaultTag.get());
    }

    @Test
    public void test_default_tag_parser() {
        final String knownDefaultTag = "knownDefaultTag";

        // object under test, default mapping with no properties
        final Properties properties = new Properties();
        properties.put(PROPERTY_DEFAULT_NODE_TAG, knownDefaultTag);

        final Mapper mapper = new Mapper(properties);

        // read test-specific mapping
        final Map<String, Object> mappings = getMapping("default_node_tag_property/mapping.json");

        final PuppetDBNode puppetNode = newPuppetDBNodeMock();

        final Optional<INodeEntry> maybeNode = mapper.apply(puppetNode, mappings);
        assertTrue("maybeNode should be present", maybeNode.isPresent());

        // assertTrue("default tag should be empty", !defaultTag.isPresent());
    }

    @Test
    public void test_no_default_tag() {
        // object under test, default mapping with no properties
        final Properties emptyProperties = new Properties();
        final Mapper mapper = new Mapper(emptyProperties);

        final Optional<String> defaultTag = mapper.getDefaultNodeTag();

        assertTrue("default tag should be empty", !defaultTag.isPresent());
    }

    @Test
    public void test_no_default_tag_parser() {
        // object under test, default mapping with no properties
        final Properties emptyProperties = new Properties();
        final Mapper mapper = new Mapper(emptyProperties);

        // read test-specific mapping
        final Map<String, Object> mappings = getMapping("default_node_tag_property/mapping.json");

        final PuppetDBNode puppetNode = newPuppetDBNodeMock();

        final Optional<INodeEntry> maybeNode = mapper.apply(puppetNode, mappings);
        assertTrue("maybeNode should be empty", !maybeNode.isPresent());

        // assertTrue("default tag should be empty", !defaultTag.isPresent());
    }

    private PuppetDBNode newPuppetDBNodeMock() {
        final PuppetDBNode puppet = Mockito.mock(PuppetDBNode.class);

        Mockito.when(puppet.getCertname()).thenReturn("certname");
        Mockito.when(puppet.getClasses()).thenReturn(new ArrayList<String>());
        Mockito.when(puppet.getFacts()).thenReturn(new HashMap<String, Object>());

        return puppet;

    }

    public Map<String, Object> getMapping(final String fileName) {
        final Type mappingType = new TypeToken<Map<String, Object>>() {
        }.getType();
        return gson.fromJson(readFile(fileName), mappingType);
    }

    public String readFile(final String name) {
        try (final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(name);
                final Scanner scanner = new Scanner(inputStream)) {
            scanner.useDelimiter("\\Z");
            return scanner.next();
        } catch (Exception ex) {
            System.err.println("can't read file: " + name);
            ex.printStackTrace(System.err);
        }

        return "";
    }

}
