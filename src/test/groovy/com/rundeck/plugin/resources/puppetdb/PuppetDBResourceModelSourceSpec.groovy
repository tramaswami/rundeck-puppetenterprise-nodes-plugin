package com.rundeck.plugin.resources.puppetdb

import com.rundeck.plugin.resources.puppetdb.client.PuppetAPI
import spock.lang.Specification
import com.rundeck.plugin.resources.puppetdb.client.model.*

/**
 * Created by greg on 3/8/16.
 */
class PuppetDBResourceModelSourceSpec extends Specification {
    def "empty nodes result"() {
        given:
        def api = Mock(PuppetAPI)
        def mapping = [:]
        def Mapper mapper = new Mapper([:] as Properties)
        def source = new PuppetDBResourceModelSource(api, mapper, mapping, true)

        when:
        def result = source.getNodes()

        then:
        1 * api.getNodes() >> []
        result != null
        result.getNodes().size() == 0


    }

    def "single node"() {
        given:
        def api = Mock(PuppetAPI)
        def mapping = [
                nodename: ["path": "certname"],
                hostname: ["default": "blah"],
                username: ["default": "user1"]
        ]
        def Mapper mapper = new Mapper([:] as Properties)
        def source = new PuppetDBResourceModelSource(api, mapper, mapping, true)

        def node = new Node()
        node.certname = 'test1'
        def nfact = new NodeFact()
        nfact.certname = 'test1'
        nfact.setEnvironment('production')
        nfact.setName('blah')
        nfact.setValue('blee')
        def nclass = new CertNodeClass(certname: 'test1', title: 'A[class]')

        def resnode = new PuppetDBNode(node,[nfact],[nclass])

        when:
        def result = source.getNodes()

        then:
        1 * api.getNodes() >> [node]
        1 * api.getFactSet(_) >> [nfact]
        1 * api.getClassesForAllNodes() >> [nclass]
        1 * api.queryNodeWithFacts(_,_)>> { args ->
            return {Node n1->resnode}
        }
        result!=null
        result.nodeNames.contains("test1")
        result.getNode("test1") != null
        result.getNode("test1").hostname == 'blah'
        result.getNode("test1").username == 'user1'

    }
}
