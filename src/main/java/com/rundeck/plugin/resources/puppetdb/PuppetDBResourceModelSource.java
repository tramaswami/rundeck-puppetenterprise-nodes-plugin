package com.rundeck.plugin.resources.puppetdb;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.common.INodeSet;
import com.dtolabs.rundeck.core.common.NodeSetImpl;
import com.dtolabs.rundeck.core.resources.ResourceModelSource;
import com.dtolabs.rundeck.core.resources.ResourceModelSourceException;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.rundeck.plugin.resources.puppetdb.client.PuppetAPI;
import com.rundeck.plugin.resources.puppetdb.client.model.Node;
import com.rundeck.plugin.resources.puppetdb.client.model.NodeFact;
import com.rundeck.plugin.resources.puppetdb.client.model.PuppetDBNode;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by greg on 3/7/16.
 */
class PuppetDBResourceModelSource implements ResourceModelSource {
    private final PuppetAPI puppetAPI;
    private final Mapper mapper;
    private final Map<String, Object> mapping;
    private boolean includeClasses;

    public PuppetDBResourceModelSource(
            final PuppetAPI puppetAPI,
            final Mapper mapper,
            final Map<String, Object> mapping,
            final boolean includeClasses
    )
    {
        this.puppetAPI = puppetAPI;
        this.mapper = mapper;
        this.mapping = mapping;
        this.includeClasses = includeClasses;
    }

    @Override
    public INodeSet getNodes() throws ResourceModelSourceException {
        // get list of nodes without filtering
        final List<Node> nodes = puppetAPI.getNodes();
        if (null == nodes || nodes.isEmpty()) {
            return new NodeSetImpl();
        }
        Set<String> factNames = mapper.determineFactNames(mapping);

        List<NodeFact> factSet = puppetAPI.getFactSet(factNames);

        // build nodes with facts and tags attached
        final List<PuppetDBNode> puppetNodes = FluentIterable.from(nodes)
                                                             .transform(puppetAPI.queryNodeWithFacts(factSet))
                                                             .toList();

        final List<INodeEntry> rundeckNodes = FluentIterable.from(puppetNodes)
                                                            .transform(mapper.withFixedMapping(mapping))
                                                            .filter(new Predicate<Optional<INodeEntry>>() {
                                                                @Override
                                                                public boolean apply(final Optional<INodeEntry> input) {
                                                                    return input.isPresent();
                                                                }
                                                            })
                                                            .transform(new Function<Optional<INodeEntry>, INodeEntry>() {

                                                                @Override
                                                                public INodeEntry apply(final Optional<INodeEntry>
                                                                                                input) {
                                                                    return input.get();
                                                                }
                                                            })
                                                            .toList();

        final NodeSetImpl nodeSet = new NodeSetImpl();
        nodeSet.putNodes(rundeckNodes);
        return nodeSet;
    }
}
