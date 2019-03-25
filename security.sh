#!/usr/bin/env bash
echo ${TEST_URL}
zap-api-scan.py -t ${TEST_URL}/v2/api-docs -f openapi -P 1001
cat zap.out
zap-cli --zap-url http://0.0.0.0 -p 1001 report -o /zap/api-report.xml -f xml
zap-cli --zap-url http://0.0.0.0 -p 1001 report -o /zap/api-report.html -f html
echo "listings of zap folder"
cat /zap/api-report.xml
ls -la /zap
cp /zap/api-report.html functional-output/
cp /zap/api-report.xml functional-output/

if [ -f zap-known-issues.xml ]; then
  if diff -q zap-known-issues.xml functional-output/api-report.xml --ignore-all-space --ignore-matching-lines=OWASPZAPReport > /dev/null 2>&1; then
    echo
    echo Ignorning known vulnerabilities
    exit 0
  fi
fi
echo
echo ZAP Security vulnerabilities were found that were not ignored
echo
echo Check to see if these vulnerabilities apply to production
echo and/or if they have fixes available. If they do not have
echo fixes and they do not apply to production, you may ignore them
echo
echo To ignore these vulnerabilities, add them to:
echo
echo "./zap-known-issues.xml"
echo
echo and commit the change

zap-cli -p 1001 alerts -l Informational