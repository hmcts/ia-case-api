provider "azurerm" {
  features {}
}

provider "azurerm" {
    features {}
    skip_provider_registration = true
    alias                      = "cft_vnet"
    subscription_id            = var.aks_subscription_id
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
  tags     = merge(var.common_tags, tomap({"lastUpdated" = "${timestamp()}"}))
}

data "azurerm_key_vault" "ia_key_vault" {
  name                = "${local.key_vault_name}"
  resource_group_name = "${local.key_vault_name}"
}

module "ia_case_api_database_11" {
  source             = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product            = "${var.product}-${var.component}-postgres-11-db"
  location           = "${var.location}"
  env                = "${var.env}"
  database_name      = "${var.postgresql_database_name}"
  postgresql_user    = "${var.postgresql_user}"
  postgresql_version = "11"
  common_tags        =  merge(var.common_tags, tomap({"lastUpdated" = "${timestamp()}"}))
  subscription       = "${var.subscription}"
  backup_retention_days = "${var.database_backup_retention_days}"
}

module "ia-case-api-db-v15" {
  providers = {
    azurerm.postgres_network = azurerm.cft_vnet
  }
  source          = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"
  env             = var.env
  product         = var.product
  component       = var.component
  business_area   = "cft"
  common_tags     = merge(var.common_tags, tomap({"lastUpdated" = "${timestamp()}"}))
  name            = "${var.product}-${var.component}-postgres-db-v15"
  pgsql_databases = [
    {
      name : var.postgresql_database_name
    }
  ]
  subnet_suffix = "expanded"
  pgsql_server_configuration = [
    {
      name  = "azure.extensions"
      value = "plpgsql,pg_stat_statements,pg_buffercache"
    }
  ]
  pgsql_version   = "15"
  admin_user_object_id = var.jenkins_AAD_objectId
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS-11" {
  name         = "${var.component}-POSTGRES-PASS-11"
  value        = module.ia_case_api_database_11.postgresql_password
  key_vault_id = data.azurerm_key_vault.ia_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS-15" {
  name         = "${var.component}-POSTGRES-PASS-15"
  value        = module.ia-case-api-db-v15.password
  key_vault_id = data.azurerm_key_vault.ia_key_vault.id
}

data "azurerm_key_vault_secret" "app_insights_connection_string" {
  name      = "ia-app-insights-connection-string"
  key_vault_id = data.azurerm_key_vault.ia_key_vault.id
}

resource "azurerm_key_vault_secret" "local_app_insights_connection_string" {
  name         = "app-insights-connection-string"
  value        = data.azurerm_key_vault_secret.app_insights_connection_string.value
  key_vault_id = data.azurerm_key_vault.ia_key_vault.id
}

resource "azurerm_key_vault_secret" "local_ia_config_validator_secret" {
  name         = "ia-config-validator-secret"
  value        = "ok"
  key_vault_id = data.azurerm_key_vault.ia_key_vault.id
}
