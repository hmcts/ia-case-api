#!/bin/zsh
if [ -z "$1" ]; then
    echo "Missing environment variable. Usage: $0 <environment>"
    exit 1
fi
environment=$1
# Construct the Key Vault name dynamically
vault_name="ia-$environment"

az login

secret_json=$(az keyvault secret show --vault-name $vault_name --name idam-secret)
secret_value=$(echo "$secret_json" | jq -r '.value')
my_secret_variable=$secret_value

# Run the curl command and capture the JSON response
response=$(curl -L -X POST "https://idam-web-public.$environment.platform.hmcts.net/o/token" \
-H 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=client_credentials' \
--data-urlencode 'client_id=iac' \
--data-urlencode "client_secret=$my_secret_variable" \
--data-urlencode 'scope=profile roles')

# Extract the access_token value using jq
access_token=$(echo "$response" | jq -r '.access_token')

# Output the access_token
echo "$access_token"