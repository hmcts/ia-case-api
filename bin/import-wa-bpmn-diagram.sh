#!/usr/bin/env bash

set -eu
workspace=${1}

serviceToken=$($(realpath $workspace)/bin/utils/idam-lease-service-token.sh iac \
  $(docker run --rm toolbelt/oathtool --totp -b ${IAC_S2S_KEY:-AABBCCDDEEFFGGHH}))

filepath="$(realpath $workspace)/resources"

for file in $(find ${filepath} -name '*.bpmn')
do
  uploadResponse=$(curl --insecure -v --silent -w "\n%{http_code}" --show-error -X POST \
    ${CAMUNDA_BASE_URL:-http://localhost:9404}/engine-rest/deployment/create \
    -H "Accept: application/json" \
    -H "ServiceAuthorization: Bearer ${serviceToken}" \
    -F "deployment-name=$(date +"%Y%m%d-%H%M%S")-$(basename ${file})" \
    -F "file=@${filepath}/$(basename ${file})")

upload_http_code=$(echo "$uploadResponse" | tail -n1)
upload_response_content=$(echo "$uploadResponse" | sed '$d')

if [[ "${upload_http_code}" == '200' ]]; then
  echo "$(basename ${file}) diagram uploaded successfully (${upload_response_content})"
  continue;
fi

echo "$(basename ${file}) upload failed with http code ${upload_http_code} and response (${upload_response_content})"
continue;

done
exit 0;
