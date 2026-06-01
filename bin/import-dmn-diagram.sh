#!/usr/bin/env bash

set -eu
workspace=${1}
tenant_id=${2}
product=${3}

s2sSecret=${IAC_S2S_KEY:-AABBCCDDEEFFGGHH}

#if [[ "${env}" == 'prod' ]]; then
#  s2sSecret=${IAC_S2S_KEY}
#fi

serviceToken=$($(realpath $workspace)/bin/utils/idam-lease-service-token.sh iac \
  $(docker run --rm hmctsprod.azurecr.io/imported/toolbelt/oathtool --totp -b ${s2sSecret}))

dmnFilepath="$(realpath $workspace)/resources"

echo "${CAMUNDA_BASE_URL} import-dmn-diagram.sh line 19"

MAX_RETRIES=6
RETRY_DELAY=5

service_up=false

for ((i=1; i<=MAX_RETRIES; i++)); do
  echo "Attempt $i of $MAX_RETRIES..."

  response=$(curl --insecure -v --silent --show-error \
    -w "\n%{http_code}" \
    -X GET "${CAMUNDA_BASE_URL:-http://localhost:9404}/health")

  http_code=$(echo "$response" | tail -n1)
  body=$(echo "$response" | sed '$d')

  status=$(echo "$body" | jq -r '.status // empty')

  echo "HTTP Status: $http_code"
  echo "Status: $status"

  if [[ "$http_code" == "200" && "$status" == "UP" ]]; then
    service_up=true
    break
  fi

  if [[ $i -lt $MAX_RETRIES ]]; then
    echo "Service not UP yet. Retrying in ${RETRY_DELAY}s..."
    sleep "$RETRY_DELAY"
  fi
done

if [[ "$service_up" == true ]]; then
  echo "do something"
else
  echo "Service did not become UP after $MAX_RETRIES attempts"
  exit 1
fi

for file in $(find ${dmnFilepath} -name '*.dmn')
do
  uploadResponse=$(curl --insecure -v --silent -w "\n%{http_code}" --show-error -X POST \
    ${CAMUNDA_BASE_URL:-http://localhost:9404}/engine-rest/deployment/create \
    -H "Accept: application/json" \
    -H "ServiceAuthorization: Bearer ${serviceToken}" \
    -F "deployment-name=$(basename ${file})" \
    -F "deploy-changed-only=true" \
    -F "deployment-source=$product" \
    ${tenant_id:+'-F' "tenant-id=$tenant_id"} \
    -F "file=@${dmnFilepath}/$(basename ${file})")

upload_http_code=$(echo "$uploadResponse" | tail -n1)
upload_response_content=$(echo "$uploadResponse" | sed '$d')

if [[ "${upload_http_code}" == '200' ]]; then
  echo "$(basename ${file}) diagram uploaded successfully (${upload_response_content})"
  continue;
fi

echo "$(basename ${file}) upload failed with http code ${upload_http_code} and response (${upload_response_content})"
continue;

done

bpmnFilepath="$(realpath $workspace)/camunda"
if [ -d ${bpmnFilepath} ]
then
  for file in $(find ${bpmnFilepath} -name '*.bpmn')
  do
    uploadResponse=$(curl --insecure -v --silent -w "\n%{http_code}" --show-error -X POST \
      ${CAMUNDA_BASE_URL:-http://localhost:9404}/engine-rest/deployment/create \
      -H "Accept: application/json" \
      -H "ServiceAuthorization: Bearer ${serviceToken}" \
      -F "deployment-name=$(basename ${file})" \
      -F "deploy-changed-only=true" \
      -F "file=@${bpmnFilepath}/$(basename ${file})")

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
fi


