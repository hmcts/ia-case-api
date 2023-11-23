variable "product" {
}

variable "raw_product" {
  default = "ia" // jenkins-library overrides product for PRs and adds e.g. pr-123-ia
}

variable "component" {
}

variable "location" {
  default = "UK South"
}

variable "env" {
}

variable "subscription" {
}

variable "ilbIp" {
  default = ""
}

variable "common_tags" {
  type = map(string)
}

variable "capacity" {
  default = "1"
}

variable "instance_size" {
  default = "I1"
}

variable "appinsights_instrumentation_key" {
  default = ""
}

variable "root_logging_level" {
  default = "INFO"
}

variable "log_level_spring_web" {
  default = "INFO"
}

variable "log_level_ia" {
  default = "INFO"
}

variable "postgresql_database_name" {
  default = "ia_case_api"
}

variable "postgresql_user" {
  default = "ia_case_api"
}

variable "database_backup_retention_days" {
  default = "35"
}

variable "aks_subscription_id" {}

variable "jenkins_AAD_objectId" {}
