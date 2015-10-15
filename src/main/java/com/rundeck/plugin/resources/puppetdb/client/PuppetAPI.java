package com.rundeck.plugin.resources.puppetdb.client;

import java.util.List;

import com.rundeck.plugin.resources.puppetdb.client.model.Node;
import com.rundeck.plugin.resources.puppetdb.client.model.NodeWithFacts;

public interface PuppetAPI {

    List<Node> getNodes();

    NodeWithFacts getFacts(final Node node);

}
