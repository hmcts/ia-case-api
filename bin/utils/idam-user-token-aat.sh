#!/bin/bash
## Usage: ./idam-user-token.sh [user] [password]
##
## Options:
##    - username: Role assigned to user in generated token. Default to `ccd-import@fake.hmcts.net`.
##    - password: ID assigned to user in generated token. Default to `London01`.
##
## Returns a valid IDAM user token for the given username and password.

USERNAME=servicesatcdmiac@gmail.com
REDIRECT_URI="https://ia-case-api-aat.service.core-compute-aat.internal/oauth2/callback"
CLIENT_ID="ccd_admin"
SCOPE="openid%20profile%20roles"

curl --silent --show-error \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -XPOST "${IDAM_API_BASE_URL}/o/token?grant_type=password&redirect_uri=${REDIRECT_URI}&client_id=${CLIENT_ID}&client_secret=${CCD_CLIENT_SECRET}&username=${USERNAME}&password=${CCD_UPLOAD_PASSWORD}&scope=${SCOPE}" -d "" | jq -r .access_token
