package com.rundeck.plugin.resources.puppetdb;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rundeck.plugin.resources.puppetdb.client.PuppetAPI;
import com.rundeck.plugin.resources.puppetdb.client.model.*;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class TestUtilities {
    private static Gson gson = new Gson();

    private TestUtilities() throws InstantiationException {
        throw new InstantiationException("Instances of this type are forbidden.");
    }

    public static String readFile(final String name) {
        try (final InputStream inputStream = TestUtilities.class.getClassLoader().getResourceAsStream(name);
             final Scanner scanner = new Scanner(inputStream)) {
            scanner.useDelimiter("\\Z");
            return scanner.next();
        } catch (Exception ex) {
            System.err.println("can't read file: " + name);
            ex.printStackTrace(System.err);
        }

        return "";
    }

    public static Map<String, Object> getMapping(final String fileName) {
        final Type mappingType = new TypeToken<Map<String, Object>>() {
        }.getType();
        return gson.fromJson(TestUtilities.readFile(fileName), mappingType);
    }

    public static PuppetAPI getPuppetApiStub(final String resourcePath) {
        return new PuppetAPI() {
            @Override
            public List<CertNodeResource> getResourcesForAllNodes(final String userQuery, final String resourceTag) {
                return null;
            }

            @Override
            public List<NodeFact> getFactSet(final Set<String> facts, final String userQuery) {
                return null;
            }

            @Override
            public List<Node> getNodes(final String userQuery) {
                return gson.fromJson(TestUtilities.readFile(resourcePath + "/nodes.json"), Node.LIST);
            }

            @Override
            public List<Fact> getFactsForNode(final Node node) {
                return gson.fromJson(TestUtilities.readFile(resourcePath + "/facts.json"), Fact.LIST);
            }

            @Override
            public List<NodeResource> getResourcesForNode(final Node node, final String resourceTag) {
                return gson.fromJson(TestUtilities.readFile(resourcePath + "/classes.json"), NodeResource.LIST);
            }
        };
    }
}
