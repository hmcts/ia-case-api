#!/usr/bin/env bash

set -eu

microservice=${1}
oneTimePassword=${2}

EVALUATED_SERVICE_AUTH_PROVIDER_API_BASE_URL="${SERVICE_AUTH_PROVIDER_API_BASE_URL:-http://localhost:4502}"

curl -v --insecure --fail --show-error --silent -X POST \
  ${EVALUATED_SERVICE_AUTH_PROVIDER_API_BASE_URL}/lease \
  -H "Content-Type: application/json" \
  -d '{
    "microservice": "'${microservice}'",
    "oneTimePassword": "'${oneTimePassword}'"
  }'
