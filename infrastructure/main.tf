# Temporary fix for template API version error on deployment
provider "azurerm" {
  version = "1.22.1"
}

locals {
  preview_app_service_plan     = "${var.product}-${var.component}-${var.env}"
  non_preview_app_service_plan = "${var.product}-${var.env}"
  app_service_plan             = "${var.env == "preview" || var.env == "spreview" ? local.preview_app_service_plan : local.non_preview_app_service_plan}"

  preview_vault_name     = "${var.raw_product}-aat"
  non_preview_vault_name = "${var.raw_product}-${var.env}"
  key_vault_name         = "${var.env == "preview" || var.env == "spreview" ? local.preview_vault_name : local.non_preview_vault_name}"
}

resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = "${var.location}"
  tags     = "${merge(var.common_tags, map("lastUpdated", "${timestamp()}"))}"
}

data "azurerm_key_vault" "ia_key_vault" {
  name                = "${local.key_vault_name}"
  resource_group_name = "${local.key_vault_name}"
}

data "azurerm_key_vault_secret" "case_documents_api_url" {
  name      = "case-documents-api-url"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "case_notifications_api_url" {
  name      = "case-notifications-api-url"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "docmosis_enabled" {
  name      = "docmosis-enabled"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "em_stitching_enabled" {
  name      = "em-stitching-enabled"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "test_caseofficer_username" {
  name      = "test-caseofficer-username"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "test_caseofficer_password" {
  name      = "test-caseofficer-password"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "test_law_firm_a_username" {
  name      = "test-law-firm-a-username"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "test_law_firm_a_password" {
  name      = "test-law-firm-a-password"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "system_username" {
  name      = "system-username"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "system_password" {
  name      = "system-password"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "idam_client_id" {
  name      = "idam-client-id"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "idam_secret" {
  name      = "idam-secret"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "idam_redirect_uri" {
  name      = "idam-redirect-uri"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "s2s_secret" {
  name      = "s2s-secret"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "s2s_microservice" {
  name      = "s2s-microservice"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "ccd_url" {
  name      = "ccd-url"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "ccd_gw_url" {
  name      = "ccd-gw-url"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "dm_url" {
  name      = "dm-url"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "idam_url" {
  name      = "idam-url"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "s2s_url" {
  name      = "s2s-url"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "hearing_centre_activation_date_bradford" {
  name      = "hearing-centre-activation-date-bradford"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "hearing_centre_activation_date_manchester" {
  name      = "hearing-centre-activation-date-manchester"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "hearing_centre_activation_date_newport" {
  name      = "hearing-centre-activation-date-newport"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "hearing_centre_activation_date_taylor_house" {
  name      = "hearing-centre-activation-date-taylor-house"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

module "ia_case_api_database" {
  source             = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product            = "${var.product}-${var.component}-postgres-db"
  location           = "${var.location}"
  env                = "${var.env}"
  database_name      = "${var.postgresql_database_name}"
  postgresql_user    = "${var.postgresql_user}"
  postgresql_version = "10"
  common_tags        = "${merge(var.common_tags, map("lastUpdated", "${timestamp()}"))}"
  subscription       = "${var.subscription}"
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name         = "${var.component}-POSTGRES-PASS"
  value        = "${module.ia_case_api_database.postgresql_password}"
  key_vault_id = "${data.azurerm_key_vault.ia_key_vault.id}"
}

module "ia_case_api" {
  source                          = "git@github.com:hmcts/cnp-module-webapp?ref=master"
  product                         = "${var.product}-${var.component}"
  location                        = "${var.location}"
  env                             = "${var.env}"
  enable_ase                      = "${var.enable_ase}"
  ilbIp                           = "${var.ilbIp}"
  resource_group_name             = "${azurerm_resource_group.rg.name}"
  subscription                    = "${var.subscription}"
  capacity                        = "${var.capacity}"
  instance_size                   = "${var.instance_size}"
  common_tags                     = "${merge(var.common_tags, map("lastUpdated", "${timestamp()}"))}"
  appinsights_instrumentation_key = "${var.appinsights_instrumentation_key}"
  asp_name                        = "${local.app_service_plan}"
  asp_rg                          = "${local.app_service_plan}"

  app_settings = {
    LOGBACK_REQUIRE_ALERT_LEVEL = false
    LOGBACK_REQUIRE_ERROR_CODE  = false

    IA_CASE_DOCUMENTS_API_URL     = "${data.azurerm_key_vault_secret.case_documents_api_url.value}"
    IA_CASE_NOTIFICATIONS_API_URL = "${data.azurerm_key_vault_secret.case_notifications_api_url.value}"
    IA_DOCMOSIS_ENABLED           = "${data.azurerm_key_vault_secret.docmosis_enabled.value}"
    IA_EM_STITCHING_ENABLED       = "${data.azurerm_key_vault_secret.em_stitching_enabled.value}"

    IA_SYSTEM_USERNAME            = "${data.azurerm_key_vault_secret.system_username.value}"
    IA_SYSTEM_PASSWORD            = "${data.azurerm_key_vault_secret.system_password.value}"
    IA_IDAM_CLIENT_ID             = "${data.azurerm_key_vault_secret.idam_client_id.value}"
    IA_IDAM_SECRET                = "${data.azurerm_key_vault_secret.idam_secret.value}"
    IA_IDAM_REDIRECT_URI          = "${data.azurerm_key_vault_secret.idam_redirect_uri.value}"
    IA_S2S_SECRET                 = "${data.azurerm_key_vault_secret.s2s_secret.value}"
    IA_S2S_MICROSERVICE           = "${data.azurerm_key_vault_secret.s2s_microservice.value}"

    POSTGRES_HOST     = "${module.ia_case_api_database.host_name}"
    POSTGRES_PORT     = "${module.ia_case_api_database.postgresql_listen_port}"
    POSTGRES_NAME     = "${module.ia_case_api_database.postgresql_database}"
    POSTGRES_USERNAME = "${module.ia_case_api_database.user_name}"
    POSTGRES_PASSWORD = "${module.ia_case_api_database.postgresql_password}"

    CCD_URL  = "${data.azurerm_key_vault_secret.ccd_url.value}"
    CCD_GW_URL = "${data.azurerm_key_vault_secret.ccd_gw_url.value}"
    DM_URL   = "${data.azurerm_key_vault_secret.dm_url.value}"
    IDAM_URL = "${data.azurerm_key_vault_secret.idam_url.value}"
    S2S_URL  = "${data.azurerm_key_vault_secret.s2s_url.value}"

    IA_HEARING_CENTRE_ACTIVATION_DATE_BRADFORD       = "${data.azurerm_key_vault_secret.hearing_centre_activation_date_bradford.value}"
    IA_HEARING_CENTRE_ACTIVATION_DATE_MANCHESTER     = "${data.azurerm_key_vault_secret.hearing_centre_activation_date_manchester.value}"
    IA_HEARING_CENTRE_ACTIVATION_DATE_NEWPORT        = "${data.azurerm_key_vault_secret.hearing_centre_activation_date_newport.value}"
    IA_HEARING_CENTRE_ACTIVATION_DATE_TAYLOR_HOUSE   = "${data.azurerm_key_vault_secret.hearing_centre_activation_date_taylor_house.value}"

    ROOT_LOGGING_LEVEL   = "${var.root_logging_level}"
    LOG_LEVEL_SPRING_WEB = "${var.log_level_spring_web}"
    LOG_LEVEL_IA         = "${var.log_level_ia}"
    EXCEPTION_LENGTH     = 100

  }
}
