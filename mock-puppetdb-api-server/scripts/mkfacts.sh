#!/bin/bash

count=${1:-3000}
fact=$2
val=${3:-}
echo '['
for i in $(seq $count) ; do
ival=${val:-$fact$i}
cat <<END
  {
"certname" : "localhost-${i}",
  "environment" : "production",
  "name" : "$fact",
  "value" : "$ival"
  },
END
done
echo ']'
