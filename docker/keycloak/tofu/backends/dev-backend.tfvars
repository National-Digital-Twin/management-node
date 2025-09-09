# SPDX-License-Identifier: Apache-2.0
# © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally attributed to the Department for Business and Trade (UK) as the governing entity.resource "keycloak_openid_client" "management_node" {


bucket  = "5371-2494-4113-state"
key     = "keycloak/01-base/dev/terraform.tfstate"
region  = "eu-west-2"
encrypt = true
