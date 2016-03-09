package com.rundeck.plugin.resources.puppetdb

import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException
import spock.lang.Specification

/**
 * Created by greg on 3/8/16.
 */
class ResourceModelFactorySpec extends Specification {
    def "validate for missing props"() {
        given:
        def properties = [
                :
        ]
        properties.put(Constants.PROPERTY_PUPPETDB_SSL_DIR, "ssl");
        properties.put(Constants.PROPERTY_PUPPETDB_HOST, "localhost");
        properties.put(Constants.PROPERTY_PUPPETDB_PORT, "8081");
        properties.remove(missingprop)


        when:
        ResourceModelFactory.validate(properties as Properties);

        then:
        ConfigurationException e = thrown()

        where:
        missingprop                      | _
        Constants.PROPERTY_PUPPETDB_HOST | _
        Constants.PROPERTY_PUPPETDB_PORT | _

    }

    def "validate no missing props"() {
        given:
        def properties = [
                :
        ]
        properties.put(Constants.PROPERTY_PUPPETDB_SSL_DIR, "ssl");
        properties.put(Constants.PROPERTY_PUPPETDB_HOST, "localhost");
        properties.put(Constants.PROPERTY_PUPPETDB_PORT, "8081");


        when:
        ResourceModelFactory.validate(properties as Properties);

        then:
        true


    }
}
