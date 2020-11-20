provider "azurerm" {
  features {}
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
  location = var.location
  tags     = merge(var.common_tags, map("lastUpdated", "${timestamp()}"))
}

data "azurerm_key_vault" "ia_key_vault" {
  name                = "${local.key_vault_name}"
  resource_group_name = "${local.key_vault_name}"
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
  value        = module.ia_case_api_database.postgresql_password
  key_vault_id = data.azurerm_key_vault.ia_key_vault.id
}

module "ia_case_api_database_11" {
  source             = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product            = "${var.product}-${var.component}-postgres-11-db"
  location           = "${var.location}"
  env                = "${var.env}"
  database_name      = "${var.postgresql_database_name}"
  postgresql_user    = "${var.postgresql_user}"
  postgresql_version = "11"
  common_tags        = "${merge(var.common_tags, map("lastUpdated", "${timestamp()}"))}"
  subscription       = "${var.subscription}"
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS-11" {
  name         = "${var.component}-POSTGRES-PASS-11"
  value        = module.ia_case_api_database_11.postgresql_password
  key_vault_id = data.azurerm_key_vault.ia_key_vault.id
}