#!/usr/bin/env bash

set -ex

dir=$(dirname ${0})

role=${1}

userToken=$(sh ${dir}/idam-user-token-aat.sh)
serviceToken=$(sh ${dir}/idam-service-token-aat.sh)

echo "Creating CCD role: ${role} using ${CCD_DEFINITION_STORE_API_BASE_URL}"

curl --insecure --fail --show-error --silent -X PUT \
  ${CCD_DEFINITION_STORE_API_BASE_URL}/api/user-role \
  -H "Authorization: Bearer ${userToken}" \
  -H "ServiceAuthorization: Bearer ${serviceToken}" \
  -H "Content-Type: application/json" \
  -d '{
    "role": "'${role}'",
    "security_classification": "PUBLIC"
  }'
