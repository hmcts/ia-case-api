#!/bin/zsh

if [ -z "$1" ]; then
    echo "Missing environment variable. Usage: $0 <environment> <email(optional)>"
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
if [ -z "$2" ]; then
    echo "Missing email so generating one automatically. Usage: $0 <environment> <email(optional)>"
    email_address="citizen-$uuid@mailnesia.com"
else
  email_address=$2
fi

json_payload=$(cat <<EOF
{
    "password": "Apassword123",
    "user": {
        "id":"$uuid",
        "email":"$email_address",
        "forename":"fn_$uuid",
        "surname":"sn_$uuid",
        "roleNames": [
            "citizen"
        ]
    }
}
EOF
)

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
