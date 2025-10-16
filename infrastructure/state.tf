terraform {
  backend "azurerm" {}

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 4.49.0"
    }
    azuread = {
      source  = "hashicorp/azuread"
      version = "3.6.0"
    }
  }
}
