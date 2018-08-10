variable "product" {
  type    = "string"
}

variable "raw_product" {
  default = "ia" // jenkins-library overrides product for PRs and adds e.g. pr-123-ia
}

variable "component" {
  type = "string"
}

variable "location" {
  type    = "string"
  default = "UK South"
}

variable "env" {
  type = "string"
}

variable "subscription" {
  type = "string"
}

variable "ilbIp"{}

variable "tenant_id" {
  description = "(Required) The Azure Active Directory tenant ID that should be used for authenticating requests to the key vault. This is usually sourced from environemnt variables and not normally required to be specified."
}

variable "client_id" {
  description = "(Required) The object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies. This is usually sourced from environment variables and not normally required to be specified."
}

variable "jenkins_AAD_objectId" {
  type = "string"
}

variable "common_tags" {
  type = "map"
}

variable "capacity" {
  default = "1"
}
