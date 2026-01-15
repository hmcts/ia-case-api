#!/bin/zsh

if [ -z "$1" ]; then
    echo "Missing environment variable. Usage: $0 <environment> <email(optional)> <roleNames(optional)>"
    exit 1
fi
environment=$1
# Ensure IDAM_TESTING_ACCESS_TOKEN is set
if [ -z "$IDAM_TESTING_ACCESS_TOKEN" ]; then
  echo "Error: IDAM_TESTING_ACCESS_TOKEN is not set. Please set the environment variable via 'export IDAM_TESTING_ACCESS_TOKEN=\$(zsh ./get_idam_token.zsh <environment>)'."
  exit 1
fi
# Generate a random UUID
uuid=$(uuidgen | tr A-F a-f)

# Check if $2 looks like a JSON array (starts with [)
if [[ "$2" == \[* ]]; then
    # $2 is roles, generate email
    email_address="citizen-$uuid@mailnesia.com"
    roles=$2
elif [ -z "$2" ]; then
    # No $2, generate email and use default roles
    echo "Missing email so generating one automatically. Usage: $0 <environment> <email(optional)> <roleNames(optional)>"
    email_address="citizen-$uuid@mailnesia.com"
    roles='["citizen"]'
else
    # $2 is email
    email_address=$2
    # Parse roleNames parameter (default to ["citizen"])
    if [ -z "$3" ]; then
        roles='["citizen"]'
    else
        roles=$3
    fi
fi

json_payload=$(cat <<EOF
{
    "password": "Apassword123",
    "user": {
        "id":"$uuid",
        "email":"$email_address",
        "forename":"fn_$uuid",
        "surname":"sn_$uuid",
        "roleNames": $roles
    }
}
EOF
)

echo "JSON Payload:"
echo "$json_payload"

# Run the curl command and capture the JSON response
response=$(curl -L -X POST "https://idam-testing-support-api.$environment.platform.hmcts.net/test/idam/users" \
-H "Authorization: Bearer $IDAM_TESTING_ACCESS_TOKEN" \
-H 'Content-Type: application/json' \
--data-raw "$json_payload")

# Extract the email value using jq
email=$(echo "$response" | jq -r '.email')

# Print the email value
echo "$response"
echo "Email: $email"
echo "Password: Apassword123"
