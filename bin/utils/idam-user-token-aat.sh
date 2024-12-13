#!/bin/bash
## Usage: ./idam-user-token.sh [user] [password]
##
## Options:
##    - username: Role assigned to user in generated token. Default to `ccd-import@fake.hmcts.net`.
##    - password: ID assigned to user in generated token. Default to `London01`.
##
## Returns a valid IDAM user token for the given username and password.

vault_name="ia-aat"

USERNAME=$IA_CCD_ADMIN_USERNAME
PASSWORD=$IA_CCD_ADMIN_PASSWORD

REDIRECT_URI="https://ia-case-api-aat.service.core-compute-aat.internal/oauth2/callback"
CLIENT_ID="ccd_admin"
CLIENT_SECRET="HoxnBugNkz9jE9QK0pvMVGjgEhBtN9Zv"
SCOPE="openid%20profile%20roles"

curl --silent --show-error \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -XPOST "${IDAM_API_BASE_URL}/o/token?grant_type=password&redirect_uri=${REDIRECT_URI}&client_id=${CLIENT_ID}&client_secret=${CLIENT_SECRET}&username=${USERNAME}&password=${PASSWORD}&scope=${SCOPE}" -d "" | jq -r .access_token
