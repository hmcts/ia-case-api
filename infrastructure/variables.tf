variable "product" {
  type = "string"
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

variable "ilbIp" {}

variable "common_tags" {
  type = "map"
}

variable "capacity" {
  default = "1"
}
