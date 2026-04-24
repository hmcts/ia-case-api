#!/usr/bin/env bash
## Usage: ./system-allocator-user-role-assignment.sh [username] [password]
##

USERNAME=${1:-system-user}
PASSWORD=${2:-system-password}

BASEDIR=$(dirname "$0")

echo "Fetching IDAM user token..."
USER_TOKEN=$($BASEDIR/idam-user-token.sh $USERNAME $PASSWORD)
echo "IDAM user token OK"

echo "Fetching IDAM user ID..."
USER_ID=$($BASEDIR/idam-user-id.sh $USER_TOKEN)
echo "User ID: ${USER_ID}"

echo "Generating TOTP and leasing S2S token..."
SERVICE_TOKEN=$($BASEDIR/idam-lease-service-token.sh iac \
  $(docker run --rm hmctsprod.azurecr.io/imported/toolbelt/oathtool --totp -b ${IAC_S2S_KEY:-AABBCCDDEEFFGGHH}))
echo "S2S token OK"

echo "Creating role assignments for user ${USER_ID} at ${ROLE_ASSIGNMENT_URL}..."

curl --silent --show-error -X POST "${ROLE_ASSIGNMENT_URL}/am/role-assignments" \
  -H "accept: application/vnd.uk.gov.hmcts.role-assignment-service.create-assignments+json;charset=UTF-8;version=1.0" \
  -H "Authorization: Bearer ${USER_TOKEN}" \
  -H "ServiceAuthorization: Bearer ${SERVICE_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
       "roleRequest": {
        "assignerId": "'"${USER_ID}"'",
         "process": "iac-system-users",
         "reference": "iac-case-allocator-system-user",
         "replaceExisting": true
       },
       "requestedRoles": [
         {
           "actorId": "'"${USER_ID}"'",
           "roleType": "ORGANISATION",
           "classification": "PUBLIC",
           "roleName": "case-allocator",
           "roleCategory": "SYSTEM",
           "grantType": "STANDARD",
           "attributes": {
             "jurisdiction": "IA",
             "primaryLocation": "765324",
             "substantive": "N"
           },
           "actorIdType": "IDAM"
         }
       ]
     }'

echo ""
echo "Done — case-allocator role assignment created."
