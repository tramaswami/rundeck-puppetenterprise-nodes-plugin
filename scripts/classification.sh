#!/usr/bin/env bash

curl -X GET http://localhost:8081/pdb/query/v4/nodes/localhost/resources/Class -v --tlsv1 \
   --insecure \
   --cacert ssl/ca/ca_crt.pem \
   --cert ssl/certs/localhost.pem \
   --key ssl/private_keys/localhost.pem

