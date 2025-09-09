# SPDX-License-Identifier: Apache-2.0
# © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally attributed to the Department for Business and Trade (UK) as the governing entity.resource "keycloak_openid_client" "management_node" {

provider "keycloak" {
  url            = var.keycloak_url
  realm          = var.keycloak_realm # manage realms from master
  client_id      = var.keycloak_client_id
  username       = var.keycloak_username
  password       = var.keycloak_password
  client_timeout = var.keycloak_client_timeout
}
