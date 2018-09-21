provider "azurerm" {}

data "azurerm_key_vault" "ia_key_vault" {
  name                = "${local.key_vault_name}"
  resource_group_name = "${local.key_vault_name}"
}

data "azurerm_key_vault_secret" "ia_case_api_url" {
  name      = "ia-case-api-url"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

locals {
  resource_group_name         = "${var.product}-${var.env}"

  preview_vault_name          = "${var.product}-aat"
  non_preview_vault_name      = "${var.product}-${var.env}"
  key_vault_name              = "${var.env == "preview" || var.env == "spreview" ? local.preview_vault_name : local.non_preview_vault_name}"

  shared_app_service_plan     = "${var.product}-aat"
  non_shared_app_service_plan = "${var.product}-${var.env}"
  app_service_plan            = "${var.env == "prod" || var.env == "sprod" ? local.non_shared_app_service_plan : local.shared_app_service_plan}"
}

module "ia_case_api" {
  source              = "git@github.com:hmcts/cnp-module-webapp?ref=master"
  product             = "${var.product}-${var.component}"
  location            = "${var.location}"
  env                 = "${var.env}"
  ilbIp               = "${var.ilbIp}"
  resource_group_name = "${local.resource_group_name}"
  subscription        = "${var.subscription}"
  capacity            = "${var.capacity}"
  common_tags         = "${merge(var.common_tags, map("lastUpdated", "${timestamp()}"))}"
  asp_name            = "${local.app_service_plan}"
  asp_rg              = "${local.app_service_plan}"

  app_settings = {
    LOGBACK_REQUIRE_ALERT_LEVEL = false
    LOGBACK_REQUIRE_ERROR_CODE  = false
    IA_CASE_API_URL             = "${data.azurerm_key_vault_secret.ia_case_api_url.value}"
  }
}
