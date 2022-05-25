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


