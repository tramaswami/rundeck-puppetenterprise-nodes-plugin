Rundeck Puppet Enterprise Nodes Plugin
========================

Version: 0.1

This is a Resource Model Source plugin for [RunDeck][] 1.5+ that provides
Puppet Enterprise Nodes as nodes for the RunDeck server.

[RunDeck]: http://rundeck.org

Installation
------------

Download from the [releases page](https://github.com/latamdevs/rundeck-puppetenterprise-nodes-plugin/releases).

Put the `rundeck-puppetenterprise-nodes-plugin-0.1.jar` into your `$RDECK_BASE/libext` dir.

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

## Filter definition

The syntax for defining filters uses `$Name=$Value[;$Name=$value[;...]]` for any of the allowed filter names (see [DescribeInstances][1] for the available filter Names).  *Note*: you do not need to specify `Filter.1.Name=$Name`, etc. as described in the Puppet Enterprise API documentation, this will handled for you.  Simply list the Name = Value pairs, separated by `;`.

 [1]: http://docs.amazonwebservices.com/AWSPuppet Enterprise/latest/APIReference/ApiReference-query-DescribeInstances.html

Example: to filter based on a Tag named "MyTag" with a value of "Some Tag Value":

    tag:MyTag=Some Tag Value
    
Example: to filter *any* instance with a Tag named `MyTag`:

    tag-key=MyTag

Example combining matching a tag value and the instance type:

    tag:MyTag=Some Tag Value;instance-type=m1.small

Mapping Definition
----------

RunDeck Node attributes are configured by mapping Puppet Enterprise Instance properties via a
mapping configuration.

The mapping declares the node attributes that will be set, and what their values
will be set to using a "selector" on properties of the Puppet Enterprise Instance object.

Here is the default mapping:

    description.default=Puppet Enterprise node instance
    editUrl.default=https://console.aws.amazon.com/Puppet Enterprise/home#s=Instances&selectInstance=${node.instanceId}
    hostname.selector=publicDnsName
    instanceId.selector=instanceId
    nodename.selector=tags/Name,instanceId
    osArch.selector=architecture
    osFamily.default=unix
    osFamily.selector=platform
    osName.default=Linux
    osName.selector=platform
    privateDnsName.selector=privateDnsName
    privateIpAddress.selector=privateIpAddress
    state.selector=state.name
    tag.pending.selector=state.name=pending
    tag.running.selector=state.name=running
    tag.shutting-down.selector=state.name=shutting-down
    tag.stopped.selector=state.name=stopped
    tag.stopping.selector=state.name=stopping
    tag.terminated.selector=state.name=terminated
    tags.default=Puppet Enterprise
    tags.selector=tags/Rundeck-Tags
    username.default=Puppet Enterprise-user
    username.selector=tags/Rundeck-User

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

### Tags selector

When defining field selector for the `tags` node property, the string value selected (if any) will
be treated as a comma-separated list of strings to use as node tags.  You could, for example, set a custom Puppet Enterprise Tag on
an instance to contain this list of tags, in this example from the simplemapping.properties file:

    tags.selector=tags/Rundeck-Tags

So creating the "Rundeck-Tags" Tag on the Puppet Enterprise Instance with a value of "alpha, beta" will result in the node having
those two node tags.

The tags.selector also supports a "merge" ability, so you can merge multiple Instance Tags into the RunDeck tags by separating multiple selectors with a "|" character:

    tags.selector=tags/Environment|tags/Role


Mapping Puppet Enterprise Instances to Rundeck Nodes
=================

Rundeck node definitions specify mainly the pertinent data for connecting to and organizing the Nodes.  Puppet Enterprise Instances have metadata that can be mapped onto the fields used for Rundeck Nodes.

Rundeck nodes have the following metadata fields:

* `nodename` - unique identifier
* `hostname` - IP address/hostname to connect to the node
* `username` - SSH username to connect to the node
* `description` - textual description
* `osName` - OS name
* `osFamily` - OS family: unix, windows, cygwin.
* `osArch` - OS architecture
* `osVersion` - OS version
* `tags` - set of labels for organization
* `editUrl` - URL to edit the definition of this node object
* `remoteUrl` - URL to edit the definition of this node object using Rundeck-specific integration

In addition, Nodes can have arbitrary attribute values.

Puppet Enterprise Instances can also have "Tags" which are key/value pairs attached to the Instance.  A common Tag is "Name" which could be a unique identifier for the Instance, making it a useful mapping to the Node's name field.  Note that Puppet Enterprise Tags differ from Rundeck Node tags: Rundeck tags are simple string labels and are not key/value pairs.

The default mapping also configures a default `username` attribute to be `Puppet Enterprise-user`, but if you want to change the default set:

`Mapping Params: ssh-keypath.default=/path/to/key;username.default=my-username`

