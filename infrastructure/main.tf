provider "azurerm" {}

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

data "azurerm_key_vault_secret" "ia_case_notifications_api_url" {
  name      = "ia-case-notifications-api-url"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "ia_idam_client_id" {
  name      = "ia-idam-client-id"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "ia_idam_secret" {
  name      = "ia-idam-secret"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "ia_idam_redirect_uri" {
  name      = "ia-idam-redirect-uri"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "ia_s2s_secret" {
  name      = "ia-s2s-secret"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "ia_s2s_microservice" {
  name      = "ia-s2s-microservice"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "ccd_url" {
  name      = "ccd-url"
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

module "ia_case_api" {
  source              = "git@github.com:hmcts/cnp-module-webapp?ref=master"
  product             = "${var.product}-${var.component}"
  location            = "${var.location}"
  env                 = "${var.env}"
  ilbIp               = "${var.ilbIp}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  subscription        = "${var.subscription}"
  capacity            = "${var.capacity}"
  instance_size       = "${var.instance_size}"
  common_tags         = "${merge(var.common_tags, map("lastUpdated", "${timestamp()}"))}"
  asp_name            = "${local.app_service_plan}"
  asp_rg              = "${local.app_service_plan}"

  app_settings = {
    LOGBACK_REQUIRE_ALERT_LEVEL = false
    LOGBACK_REQUIRE_ERROR_CODE  = false

    IA_CASE_NOTIFICATIONS_API_URL = "${data.azurerm_key_vault_secret.ia_case_notifications_api_url.value}"
    IA_IDAM_CLIENT_ID             = "${data.azurerm_key_vault_secret.ia_idam_client_id.value}"
    IA_IDAM_SECRET                = "${data.azurerm_key_vault_secret.ia_idam_secret.value}"
    IA_IDAM_REDIRECT_URI          = "${data.azurerm_key_vault_secret.ia_idam_redirect_uri.value}"
    IA_S2S_SECRET                 = "${data.azurerm_key_vault_secret.ia_s2s_secret.value}"
    IA_S2S_MICROSERVICE           = "${data.azurerm_key_vault_secret.ia_s2s_microservice.value}"

    CCD_URL  = "${data.azurerm_key_vault_secret.ccd_url.value}"
    DM_URL   = "${data.azurerm_key_vault_secret.dm_url.value}"
    IDAM_URL = "${data.azurerm_key_vault_secret.idam_url.value}"
    S2S_URL  = "${data.azurerm_key_vault_secret.s2s_url.value}"
  }
}
