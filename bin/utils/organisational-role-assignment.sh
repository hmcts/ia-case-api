#!/usr/bin/env bash
## Usage: ./organisational-role-assignment.sh [username] [password] [role_classification] [role_name] [role_attributes] [microservice_name]
##
## Options:
##    - username: Email for user. Default to `ccd-import@fake.hmcts.net`.
##    - password: Password for user. Default to `London01`.
##    - role_classification: Role assignment classification. Default to `PUBLIC`.
##    - role_name: Name of the role for role-assignment. Default to `tribunal-caseworker`.
##    - microservice_name: Name of the microservice to obtain S2S token. Default to `ccd_gw`.
##

USERNAME=${1:-ccd-import@fake.hmcts.net}
PASSWORD=${2:-London01}
ROLE_CLASSIFICATION="${3:-PUBLIC}"
ROLE_NAME="${4:-"tribunal-caseworker"}"
ROLE_ATTRIBUTES="${5:-'{"jurisdiction":"IA"}'}"
ROLE_CATEGORY="${6:-"LEGAL_OPERATIONS"}"
AM_PROCESS="${7:-"staff-organisational-role-mapping"}"

BASEDIR=$(dirname "$0")

USER_TOKEN=$($BASEDIR/idam-user-token.sh $USERNAME $PASSWORD)
USER_ID=$($BASEDIR/idam-user-id.sh $USER_TOKEN)
SERVICE_TOKEN=$($BASEDIR/idam-lease-service-token.sh iac \
  $(docker run --rm hmctspublic.azurecr.io/imported/toolbelt/oathtool --totp -b ${IAC_S2S_KEY:-AABBCCDDEEFFGGHH}))

AM_REFERENCE="${8:-${USER_ID}/${ROLE_NAME}}"

echo "\n\nCreating role assignment: \n User: ${USER_ID}\n Role name: ${ROLE_NAME}\n ROLE_CLASSIFICATION: ${ROLE_CLASSIFICATION}\n"
echo "\n\nROLE ASSIGNMENT URL: \n Url: ${ROLE_ASSIGNMENT_URL}\n"

curl --silent --show-error -X POST "${ROLE_ASSIGNMENT_URL}/am/role-assignments" \
  -H "accept: application/vnd.uk.gov.hmcts.role-assignment-service.create-assignments+json;charset=UTF-8;version=1.0" \
  -H "Authorization: Bearer ${USER_TOKEN}" \
  -H "ServiceAuthorization: Bearer ${SERVICE_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{ "roleRequest": {
          "assignerId": "'"${USER_ID}"'",
          "process": "'"${AM_PROCESS}"'",
          "reference": "'"${AM_REFERENCE}"'",
          "replaceExisting": true
        },
        "requestedRoles": [
          {
            "actorIdType": "IDAM",
            "actorId": "'"${USER_ID}"'",
            "roleType": "ORGANISATION",
            "roleName": "'"${ROLE_NAME}"'",
            "classification": "'"${ROLE_CLASSIFICATION}"'",
            "grantType": "STANDARD",
            "roleCategory": "'"${ROLE_CATEGORY}"'",
            "readOnly": false,
            "attributes": '${ROLE_ATTRIBUTES}'
          }
        ]
      }'

