package com.rundeck.plugin.resources.puppetdb.client;

import java.util.List;
import java.util.Set;

import com.rundeck.plugin.resources.puppetdb.client.model.CertNodeClass;
import com.rundeck.plugin.resources.puppetdb.client.model.Fact;
import com.rundeck.plugin.resources.puppetdb.client.model.Node;
import com.rundeck.plugin.resources.puppetdb.client.model.NodeClass;
import com.rundeck.plugin.resources.puppetdb.client.model.NodeFact;


public interface PuppetAPI {

    /**
     * List all queried nodes
     * @return
     * @param userQuery
     */
    public List<Node> getNodes(final String userQuery);

    /**
     * List all facts for node
     * @param node
     * @return
     */
    public List<Fact> getFactsForNode(final Node node);

    /**
     * List all classes for node
     * @param node
     * @return
     */
    public List<NodeClass> getClassesForNode(final Node node);

    /**
     * Get classes for all queried nodes
     * @return
     * @param userQuery
     */
    public List<CertNodeClass> getClassesForAllNodes(final String userQuery);

    /**
     * Get all selected facts for the queried nodes
     * @param facts
     * @param userQuery
     * @return
     */
    public List<NodeFact> getFactSet(Set<String> facts, final String userQuery);

}
