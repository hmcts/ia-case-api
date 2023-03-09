#!/bin/bash
## Usage: ./idam-user-token.sh [user] [password]
##
## Options:
##    - username: Role assigned to user in generated token. Default to `ccd-import@fake.hmcts.net`.
##    - password: ID assigned to user in generated token. Default to `London01`.
##
## Returns a valid IDAM user token for the given username and password.

USERNAME=servicesatcdmiac@gmail.com
PASSWORD=IacConfig29
REDIRECT_URI="https://ia-case-api-aat.service.core-compute-aat.internal/oauth2/callback"
CLIENT_ID="ccd_admin"
CLIENT_SECRET="HoxnBugNkz9jE9QK0pvMVGjgEhBtN9Zv"
SCOPE="openid%20profile%20roles"

##code=$(curl --silent --show-error -u "${USERNAME}:${PASSWORD}" -XPOST "https://idam-api.aat.platform.hmcts.net/oauth2/authorize?redirect_uri=${REDIRECT_URI}&response_type=code&client_id=${CLIENT_ID}" -d "" | jq -r .code)

##curl --silent --show-error \
##    -H "Content-Type: application/x-www-form-urlencoded" \
##    -u "${CLIENT_ID}:${CLIENT_SECRET}" \
##    -X POST "https://idam-api.aat.platform.hmcts.net/oauth2/token?code=${code}&redirect_uri=${REDIRECT_URI}&grant_type=authorization_code" -d "" | jq -r .access_token

curl --silent --show-error \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -XPOST "https://idam-api.aat.platform.hmcts.net/o/token?grant_type=password&redirect_uri=${REDIRECT_URI}&client_id=${CLIENT_ID}&client_secret=${CLIENT_SECRET}&username=${USERNAME}&password=${PASSWORD}&scope=${SCOPE}" -d "" | jq -r .access_token
