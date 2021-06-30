#!/usr/bin/env bash

#setting encoding for Python 2 / 3 compatibilities
export TEST_URL=http://ia-case-api-aat.service.core-compute-aat.internal
echo ${TEST_URL}
zap-api-scan.py -t ${TEST_URL}/v2/api-docs -f openapi -S -d -u ${SecurityRules} -P 1001 -l FAIL
curl --fail http://0.0.0.0:1001/OTHER/core/other/jsonreport/?formMethod=GET --output report.json
export LC_ALL=C.UTF-8
export LANG=C.UTF-8
zap-cli --zap-url http://0.0.0.0 -p 1001 report -o /Zap/api-report.html -f html
zap-cli --zap-url http://0.0.0.0 -p 1001 alerts -l Medium --exit-code False

cp report.json functional-output/
cp /Zap/api-report.html functional-output/

