#!/usr/bin/env bash
## Usage: ./system-user-role-assignment.sh [username] [password]
##

USERNAME=${1:-system-user}
PASSWORD=${2:-system-password}

BASEDIR=$(dirname "$0")

USER_TOKEN=$($BASEDIR/idam-user-token.sh $USERNAME $PASSWORD)
USER_ID=$($BASEDIR/idam-user-id.sh $USER_TOKEN)
SERVICE_TOKEN=$($BASEDIR/idam-lease-service-token.sh iac \
  $(docker run --rm hmctspublic.azurecr.io/imported/toolbelt/oathtool --totp -b ${IAC_S2S_KEY:-AABBCCDDEEFFGGHH}))

echo "\n\nCreating role assignment: \n User: ${USER_ID}\n Role name: ${ROLE_NAME}\n ROLE_CLASSIFICATION: ${ROLE_CLASSIFICATION}\n"
echo "\n\nROLE ASSIGNMENT URL: \n Url: ${ROLE_ASSIGNMENT_URL}\n"

curl --silent --show-error -X POST "${ROLE_ASSIGNMENT_URL}/am/role-assignments" \
  -H "accept: application/vnd.uk.gov.hmcts.role-assignment-service.create-assignments+json;charset=UTF-8;version=1.0" \
  -H "Authorization: Bearer ${USER_TOKEN}" \
  -H "ServiceAuthorization: Bearer ${SERVICE_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
       "roleRequest": {
         "process": "iac-system-users",
         "reference": "iac-hearings-system-user",
         "replaceExisting": true
       },
       "requestedRoles": [
         {
           "actorId": "'"${USER_ID}"'",
           "roleType": "ORGANISATION",
           "classification": "PUBLIC",
           "roleName": "hearing-manager",
           "roleCategory": "SYSTEM",
           "grantType": "STANDARD",
           "attributes": {
             "jurisdiction": "IA",
             "caseType": "Asylum"
           },
           "actorIdType": "IDAM"
         },
         {
           "actorId": "'"${USER_ID}"'",
           "roleType": "ORGANISATION",
           "classification": "PUBLIC",
           "roleName": "hearing-viewer",
           "roleCategory": "SYSTEM",
           "grantType": "STANDARD",
           "attributes": {
             "jurisdiction": "IA",
             "caseType": "Asylum"
           },
           "actorIdType": "IDAM"
         },
         {
           "actorId": "'"${USER_ID}"'",
           "roleType": "ORGANISATION",
           "classification": "PUBLIC",
           "roleName": "hearing-manager",
           "roleCategory": "SYSTEM",
           "grantType": "STANDARD",
           "attributes": {
             "jurisdiction": "IA",
             "caseType": "Bail"
           },
           "actorIdType": "IDAM"
         },
         {
           "actorId": "'"${USER_ID}"'",
           "roleType": "ORGANISATION",
           "classification": "PUBLIC",
           "roleName": "hearing-viewer",
           "roleCategory": "SYSTEM",
           "grantType": "STANDARD",
           "attributes": {
             "jurisdiction": "IA",
             "caseType": "Bail"
           },
           "actorIdType": "IDAM"
         },
         {
           "actorId": "'"${USER_ID}"'",
           "roleType": "ORGANISATION",
           "classification": "PUBLIC",
           "roleName": "case-allocator",
           "roleCategory": "SYSTEM",
           "grantType": "STANDARD",
           "attributes": {
             "jurisdiction": "IA",
             "primaryLocation": "765324"
           },
           "actorIdType": "IDAM"
         }
       ]
     }'

