terraform {
  backend "azurerm" {}

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 4.22.0"
    }
    azuread = {
      source  = "hashicorp/azuread"
      version = "3.1.0"
    }
  }
}
