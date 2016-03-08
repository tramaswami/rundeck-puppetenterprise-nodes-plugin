#!/bin/bash

count=${1:-3000}
echo '['
for i in $(seq $count) ; do
ival=${val:-$fact$i}
cat <<END
  {
"certname" : "localhost-${i}",
  "title" : "AClass[something]"
  },
END
done
echo ']'
