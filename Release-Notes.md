Release Notes
=========

## 0.9.x

Date: 09/03/2016

* Speed improvements when result set has thousands of nodes:
	* does not make queries for facts/classes for *every* node, it now
		only queries the `/facts/<name>` for the facts used in the mapping, and `/resources/Class` endpoint only
		if "include classes" is enabled.
* other improvements and fixes
	* does not require classes/tag for nodes
	* reduce console warning/error messages
	* fix metrics counting
	* additional refactoring and cleanup


## 0.2.x

Original releases by <https://github.com/latamdevs/rundeck-puppetenterprise-nodes-plugin>.

### 0.2

Date: 21/10/2015

Changes:

* Fix Issue with Java 1.7 compatibility

### 0.1

Initial release

Date: 20/10/2015
