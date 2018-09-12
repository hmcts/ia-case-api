provider "azurerm" {}

resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = "${var.location}"
  tags     = "${merge(var.common_tags, map("lastUpdated", "${timestamp()}"))}"
}

data "azurerm_key_vault" "ia_key_vault" {
  name                = "${local.vaultName}"
  resource_group_name = "${local.vaultName}"
}

data "azurerm_key_vault_secret" "ia_case_api_url" {
  name      = "ia-case-api-url"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

locals {
  ase_name            = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"
  previewVaultName    = "${var.raw_product}-aat"
  nonPreviewVaultName = "${var.raw_product}-${var.env}"
  vaultName           = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName}"
}

module "ia-case-api" {
  source              = "git@github.com:hmcts/moj-module-webapp?ref=master"
  product             = "${var.product}-${var.component}"
  location            = "${var.location}"
  env                 = "${var.env}"
  ilbIp               = "${var.ilbIp}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  subscription        = "${var.subscription}"
  capacity            = "${var.capacity}"
  common_tags         = "${var.common_tags}"
  asp_rg              = "${var.product}-${var.component}-${var.env}"
  asp_name            = "${var.product}-${var.component}-${var.env}"

  app_settings = {
    LOGBACK_REQUIRE_ALERT_LEVEL = false
    LOGBACK_REQUIRE_ERROR_CODE  = false
    IA_CASE_API_URL             = "${data.azurerm_key_vault_secret.ia_case_api_url.value}"
  }
}
