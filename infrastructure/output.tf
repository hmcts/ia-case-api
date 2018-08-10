output "microserviceName" {
  value = "${var.component}"
}

output "vaultUri" {
  value = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

output "vaultName" {
  value = "${local.vaultName}"
}
