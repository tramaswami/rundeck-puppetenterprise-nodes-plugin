#!/usr/bin/env bash

curl -X GET https://localhost:8081/pdb/query/v4/fact-names --tlsv1 \
   --insecure \
   --cacert ssl/ca/ca_crt.pem \
   --cert ssl/certs/localhost.pem \
   --key ssl/private_keys/localhost.pem
