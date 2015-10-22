Rundeck Puppet Enterprise Nodes Plugin
========================

Version: 0.2

This is a Resource Model Source plugin for [RunDeck][] 1.5+ that provides
Puppet Enterprise Nodes as nodes for the RunDeck server.

[RunDeck]: http://rundeck.org

Installation
------------

Download from the [releases page](https://github.com/latamdevs/rundeck-puppetenterprise-nodes-plugin/releases).

Put the `rundeck-puppetenterprise-nodes-plugin-x.y.jar` into your `$RDECK_BASE/libext` dir.

Usage
-----

You can configure the Resource Model Sources for a project either via the
RunDeck GUI, under the "Admin" page, or you can modify the `project.properties`
file to configure the sources.

See: [Resource Model Source Configuration](http://rundeck.org/1.5/manual/plugins.html#resource-model-source-configuration)

The provider name is: `puppet-enterprise`

Here are the configuration properties:

* `host`: Puppet Enterprise Master host
* `port`: Puppet Enterprise Master port
* `sslDir`: name of the ssl dir containing puppet certificates
* `mappingFile`: Path to a java properties-formatted mapping definition file.

Mapping Definition
----------

RunDeck Node attributes are configured by mapping Puppet Enterprise Node facts via a
mapping configuration.

The mapping declares the node attributes that will be set, and what their values
will be set to using a "selector" on properties of the Puppet Enterprise Node object.

the default mapping file is src/main/resources/defaultMapping.json

Configuring the Mapping
-----------------------

You can configure your source to start with the above default mapping with the 
`useDefaultMapping` property.

You can then selectively change it either by setting the `mappingParams` or 
pointing to a new properties file with `mappingFile`.

For example, you can put this in the `mappingParams` field in the GUI to change 
the default tags for your nodes, remove the "stopping" tag selector, and add a
new "ami_id" selector:

    tags.default=mytag, mytag2;tag.stopping.selector=;ami_id.selector=imageId

Mapping format
---------------

The mapping consists of defining either a selector or a default for
the desired Node fields.  The "nodename" field is required, and will 
automatically be set to the instance ID if no other value is defined.

For purposes of the mapping definition, a `field selector` is either:

* An Puppet Enterprise fieldname, or dot-separated field names
* "tags/" followed by a Tag name, e.g. "tags/My Tag"
* "tags/*" for use by the `attributes.selector` mapping

Selectors use the Apache [BeanUtils](http://commons.apache.org/beanutils/) to extract a property value from the AWS API
[Instance class](http://docs.amazonwebservices.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/Puppet Enterprise/model/Instance.html).
This means you can use dot-separated fieldnames to traverse the object graph.
E.g. "state.name" to specify the "name" field of the State property of the Instance.

format:

    # define a selector for "property":
    <attribute>.selector=<field selector>
    # define a default value for "property":
    <attribute>.default=<default value>
    # Special attributes selector to map all Tags to attributes
    attributes.selector=tags/*
    # The value for the tags selector will be treated as a comma-separated list of strings
    tags.selector=<field selector>
    # the default tags list
    tags.default=a,b,c
    # Define a single tag <name> which will be set if and only if the selector result is not empty
    tag.<name>.selector=<field selector>
    # Define a single tag <name> which will be set if the selector result equals the <value>
    tag.<name>.selector=<field selector>=<value>

Note, a ".selector" value can have multiple selectors defined, separated by commas,
and they will be evaluated in order with the first value available being used.  E.g. "nodename.selector=tags/Name,instanceId", which will look for a tag named "Name", otherwise use the instanceId.

You can also use the `<field selector>=<value>` feature to set a tag only if the field selector has a certain value.


Mapping Puppet Enterprise Nodes to Rundeck Nodes
=================

Rundeck Nodes can have arbitrary attribute values, this Plugin turns Puppet Facts (key/value) into Rundeck Attributes

Puppet Enterprise Nodes can also have "Classes" which are values attached to the Node, this Plugin associate them to Rundeck tags as they are simple string labels.
