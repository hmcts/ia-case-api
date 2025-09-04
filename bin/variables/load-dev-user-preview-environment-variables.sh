#!/usr/bin/env bash

set -eu
user=$(whoami)
echo "User directory: /Users/$user"

source .env.local

export ENVIRONMENT=preview
# urls
export URL=$XUI_WEBAPP_URL
export SERVICE_AUTH_PROVIDER_API_BASE_URL="http://rpe-service-auth-provider-aat.service.core-compute-aat.internal"
export IDAM_API_BASE_URL="https://idam-api.aat.platform.hmcts.net"
export IDAM_API_URL="https://idam-api.aat.platform.hmcts.net"
export CCD_IDAM_REDIRECT_URL="https://ccd-case-management-web-aat.service.core-compute-aat.internal/oauth2redirect"
export CCD_DEFINITION_STORE_API_BASE_URL="https://ccd-definition-store-ia-case-api-$user-pr-1.preview.platform.hmcts.net"
export ROLE_ASSIGNMENT_URL="https://am-role-assignment-ia-case-api-$user-pr-1.preview.platform.hmcts.net"
export CAMUNDA_BASE_URL="https://camunda-ia-case-api-$user.preview.platform.hmcts.net"
export SERVICES_WORK_ALLOCATION_TASK_API="https://wa-task-management-api-ia-case-api-$user-pr-1.preview.platform.hmcts.net"
export SERVICES_WA_WORKFLOW_API_URL="https://wa-workflow-api-ia-case-api-$user-pr-1.preview.platform.hmcts.net"
export WA_SUPPORTED_JURISDICTIONS="IA"

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
export TEST_JUDGE_X_USERNAME=$(az keyvault secret show --vault-name ia-aat --name test-judge-x-username --query value -o tsv)
export TEST_JUDGE_X_PASSWORD=$(az keyvault secret show --vault-name ia-aat --name test-judge-x-password --query value -o tsv)
export IA_WA_ADMINOFFICER_NO_IDAM_USERNAME=$(az keyvault secret show --vault-name ia-aat --name wa-test-adminofficer-no-idam-username  --query value -o tsv)
export IA_WA_ADMINOFFICER_NO_IDAM_PASSWORD=$(az keyvault secret show --vault-name ia-aat --name wa-test-adminofficer-no-idam-password  --query value -o tsv)
export IA_WA_CASEOFFICER_NO_IDAM_USERNAME=$(az keyvault secret show --vault-name ia-aat --name wa-test-caseofficer-no-idam-username  --query value -o tsv)
export IA_WA_CASEOFFICER_NO_IDAM_PASSWORD=$(az keyvault secret show --vault-name ia-aat --name wa-test-caseofficer-no-idam-password  --query value -o tsv)
export IA_WA_JUDGE_NO_IDAM_USERNAME=$(az keyvault secret show --vault-name ia-aat --name wa-test-judge-no-idam-username  --query value -o tsv)
export IA_WA_JUDGE_NO_IDAM_PASSWORD=$(az keyvault secret show --vault-name ia-aat --name wa-test-judge-no-idam-password  --query value -o tsv)
export IA_SYSTEM_USERNAME=$(az keyvault secret show --vault-name ia-aat --name system-username --query value -o tsv)
export IA_SYSTEM_PASSWORD=$(az keyvault secret show --vault-name ia-aat --name system-password --query value -o tsv)

# rpx-aat
export IDAM_CLIENT_SECRET=$(az keyvault secret show --vault-name rpx-aat --name mc-idam-client-secret --query value -o tsv)
