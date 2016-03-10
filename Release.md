# 0.9.x

* Speed improvements when result set has thousands of nodes:
	* does not make queries for facts/classes for *every* node, it now
		only queries the `/facts/<name>` for the facts used in the mapping, and `/resources/Class` endpoint only
		if "include classes" is enabled.
* other improvements and fixes
	* does not require classes/tag for nodes
	* reduce console warning/error messages
	* fix metrics counting
	* additional refactoring and cleanup


# 0.2.x

Original releases by <https://github.com/latamdevs/rundeck-puppetenterprise-nodes-plugin>.