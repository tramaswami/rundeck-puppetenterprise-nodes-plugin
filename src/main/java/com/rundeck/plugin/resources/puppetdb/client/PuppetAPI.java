package com.rundeck.plugin.resources.puppetdb.client;

import java.util.List;

import com.rundeck.plugin.resources.puppetdb.client.model.Fact;
import com.rundeck.plugin.resources.puppetdb.client.model.Node;

public interface PuppetAPI {

    List<Node> getNodes();

    List<Fact> getFacts(final Node node);

}
