terraform {
  backend "azurerm" {}

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = ">= 3.93.0"
    }
    azuread = {
      source  = "hashicorp/azuread"
      version = "2.33.0"
    }
  }
}
