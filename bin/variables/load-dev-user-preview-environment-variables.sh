#!/usr/bin/env bash

set -eu
user=$(whoami)
echo "User directory: /Users/$user"

source .env.local

export ENVIRONMENT=preview
# urls
export URL=$XUI_WEBAPP_URL
#export CIVIL_SERVICE_URL=$JAVA_URL
export SERVICE_AUTH_PROVIDER_API_BASE_URL="http://rpe-service-auth-provider-aat.service.core-compute-aat.internal"
export IDAM_API_BASE_URL="https://idam-api.aat.platform.hmcts.net"
export IDAM_API_URL="https://idam-api.aat.platform.hmcts.net"
export CCD_IDAM_REDIRECT_URL="https://ccd-case-management-web-aat.service.core-compute-aat.internal/oauth2redirect"
#export CCD_DEFINITION_STORE_API_BASE_URL=${CCD_DEFINITION_STORE_API_URL:-"http://ccd-definition-store-api-aat.service.core-compute-aat.internal"}
export CCD_DEFINITION_STORE_API_BASE_URL="http://ccd-definition-store-api-aat.service.core-compute-aat.internal"

# from civil
export CAMUNDA_BASE_URL=$CAMUNDA_URL
#export DEFINITION_IMPORTER_USERNAME=$(az keyvault secret show --vault-name civil-aat --name ccd-importer-username --query value -o tsv)
#export DEFINITION_IMPORTER_PASSWORD=$(az keyvault secret show --vault-name civil-aat --name ccd-importer-password --query value -o tsv)
#export CCD_CONFIGURER_IMPORTER_USERNAME=$DEFINITION_IMPORTER_USERNAME
#export CCD_CONFIGURER_IMPORTER_PASSWORD=$DEFINITION_IMPORTER_PASSWORD

# ccd-aat
export CCD_API_GATEWAY_IDAM_CLIENT_SECRET=$(az keyvault secret show --vault-name ccd-aat --name ccd-api-gateway-oauth2-client-secret --query value -o tsv)
export ADMIN_WEB_IDAM_SECRET=$(az keyvault secret show --vault-name ccd-aat --name ccd-admin-web-oauth2-client-secret --query value -o tsv)
export IDAM_OAUTH2_DATA_STORE_CLIENT_SECRET=$(az keyvault secret show --vault-name ccd-aat --name idam-data-store-client-secret --query value -o tsv)
export IDAM_DATA_STORE_SYSTEM_USER_USERNAME=$(az keyvault secret show --vault-name ccd-aat --name idam-data-store-system-user-username --query value -o tsv)
export IDAM_DATA_STORE_SYSTEM_USER_PASSWORD=$(az keyvault secret show --vault-name ccd-aat --name idam-data-store-system-user-password --query value -o tsv)
export ADDRESS_LOOKUP_TOKEN=$(az keyvault secret show --vault-name ccd-aat --name postcode-info-address-lookup-token --query value -o tsv)

# s2s-aat
export DATA_STORE_S2S_KEY=$(az keyvault secret show --vault-name s2s-aat --name microservicekey-ccd-data --query value -o tsv)
export DEFINITION_STORE_S2S_KEY=$(az keyvault secret show --vault-name s2s-aat --name microservicekey-ccd-definition --query value -o tsv)
export API_GATEWAY_S2S_KEY=$(az keyvault secret show --vault-name s2s-aat --name microservicekey-ccd-gw --query value -o tsv)
export ADMIN_S2S_KEY=$(az keyvault secret show --vault-name s2s-aat --name microservicekey-ccd-admin --query value -o tsv)
export IAC_S2S_KEY=$(az keyvault secret show --vault-name s2s-aat --name microservicekey-iac --query value -o tsv)

# ia-aat
export IA_CCD_ADMIN_USERNAME=$(az keyvault secret show --vault-name ia-aat --name ccd-admin-web-username --query value -o tsv)
export IA_CCD_ADMIN_PASSWORD=$(az keyvault secret show --vault-name ia-aat --name ccd-admin-web-password --query value -o tsv)


# for org roles
export IA_WA_ADMINOFFICER_USERNAME=$(az keyvault secret show --vault-name ia-aat --name wa-test-adminofficer-username --query value -o tsv)
export IA_WA_ADMINOFFICER_PASSWORD=$(az keyvault secret show --vault-name ia-aat --name wa-test-adminofficer-password --query value -o tsv)
export IA_WA_CASEOFFICER_USERNAME=$(az keyvault secret show --vault-name ia-aat --name wa-test-caseofficer-a-username --query value -o tsv)
export IA_WA_CASEOFFICER_PASSWORD=$(az keyvault secret show --vault-name ia-aat --name wa-test-caseofficer-a-password --query value -o tsv)
export IA_WA_CTSC_ADMIN_USERNAME=$(az keyvault secret show --vault-name ia-aat --name wa-test-ctsc-admin-username --query value -o tsv)
export IA_WA_CTSC_ADMIN_PASSWORD=$(az keyvault secret show --vault-name ia-aat --name wa-test-ctsc-admin-password --query value -o tsv)
export IA_WA_ADMINOFFICER_USERNAME_1=$(az keyvault secret show --vault-name ia-aat --name wa-test-adminofficer-username-1 --query value -o tsv)
export IA_WA_ADMINOFFICER_PASSWORD_1=$(az keyvault secret show --vault-name ia-aat --name wa-test-adminofficer-password-1 --query value -o tsv)
export IA_WA_CASEOFFICER_USERNAME_STCW=$(az keyvault secret show --vault-name ia-aat --name wa-test-caseofficer-stcw-username --query value -o tsv)
export IA_WA_CASEOFFICER_PASSWORD_STCW=$(az keyvault secret show --vault-name ia-aat --name wa-test-caseofficer-stcw-password --query value -o tsv)
export IA_WA_JUDGE_USERNAME=$(az keyvault secret show --vault-name ia-aat --name wa-test-judge-username --query value -o tsv)
export IA_WA_JUDGE_PASSWORD=$(az keyvault secret show --vault-name ia-aat --name wa-test-judge-password --query value -o tsv)
export IA_WA_CTSC_ADMIN_USERNAME_2=$(az keyvault secret show --vault-name ia-aat --name wa-test-ctsc-admin-username-2 --query value -o tsv)
export IA_WA_CTSC_ADMIN_PASSWORD_2=$(az keyvault secret show --vault-name ia-aat --name wa-test-ctsc-admin-password-2 --query value -o tsv)


#civil s2s-aat
export S2S_SECRET=$(az keyvault secret show --vault-name civil-aat --name microservicekey-civil-service --query value -o tsv)
export HEALTH_WORK_ALLOCATION_TASK_API=TBD

# definition placeholders
#export CCD_DEF_CASE_SERVICE_BASE_URL=$JAVA_URL
#export CCD_DEF_GEN_APP_SERVICE_BASE_URL=$GA_URL
#export CCD_DEF_VERSION="-${user}-1"


