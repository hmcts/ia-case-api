terraform {
  backend "azurerm" {}

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = ">= 3.93.0"
    }
    azuread = {
      source  = "hashicorp/azuread"
      version = "1.6.0"
    }
  }
}
