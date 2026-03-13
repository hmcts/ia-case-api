terraform {
  backend "azurerm" {}

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 4.64.0"
    }
    azuread = {
      source  = "hashicorp/azuread"
      version = "3.8.0"
    }
  }
}
