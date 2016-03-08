#!/bin/bash

echo '['
for i in $(seq 3000) ; do
cat <<END
  {
"certname" : "localhost-${i}",
  "environment" : "production",
  "name" : "ipaddress",
  "value" : "hostname-${i}"
  },
END
done
echo ']'
