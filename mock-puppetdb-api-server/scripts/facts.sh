#!/bin/bash

echo '['
for i in $(seq 3000) ; do
cat <<END
  {
"certname" : "localhost-${i}",
  "environment" : "production",
  "name" : "os",
  "value" : {
    "architecture" : "x86_64",
    "family" : "RedHat",
    "hardware" : "x86_64",
    "name" : "RedHat",
    "release" : {
      "full" : "7.1",
      "major" : "7",
      "minor" : "1"
    },
    "selinux" : {
      "config_mode" : "permissive",
      "config_policy" : "targeted",
      "current_mode" : "permissive",
      "enabled" : true,
      "enforced" : false,
      "policy_version" : "28"
    }
  }
},
END
done
echo ']'