# SPDX-License-Identifier: Apache-2.0
# © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally attributed to the Department for Business and Trade (UK) as the governing entity.resource "keycloak_openid_client" "management_node" {

resource "keycloak_openid_client" "management_node" {
  realm_id                     = keycloak_realm.management-node.id
  client_id                    = "management-node"
  name                         = "Management Node"
  description                  = "Management Node Client"
  enabled                      = true
  access_type                  = "CONFIDENTIAL"
  standard_flow_enabled        = false # disable browser-based auth
  direct_access_grants_enabled = false
  service_accounts_enabled     = true # allow use of client credentials

  # Per-client JWT access token lifespan (seconds)
  access_token_lifespan = var.client_access_token_lifespan_seconds
}


# Create custom client roles for the management-node client
resource "keycloak_role" "access_consumer_configurations" {
  realm_id    = keycloak_realm.management-node.id
  client_id   = keycloak_openid_client.management_node.id
  name        = "access_consumer_configurations"
  description = "Allows access to consumer configuration resources"
}

resource "keycloak_role" "access_producer_configurations" {
  realm_id    = keycloak_realm.management-node.id
  client_id   = keycloak_openid_client.management_node.id
  name        = "access_producer_configurations"
  description = "Allows access to producer configuration resources"
}

# Configure clients from federator_clients variable
module "federator_client" {
  source = "./modules/federator_client"

  for_each = { for c in var.federator_clients : c.client => c }

  client_id = each.value.client
  realm_id  = keycloak_realm.management-node.id

  # Ensure base client and its roles exist before mapping
  depends_on = [
    keycloak_openid_client.management_node,
    keycloak_role.access_consumer_configurations,
    keycloak_role.access_producer_configurations,
  ]

  # Token lifespan for this client
  client_access_token_lifespan_seconds = var.client_access_token_lifespan_seconds

  # Reuse the same default client scopes as the other federator clients
  default_client_scopes = [
    "FEDERATOR_CONSUMER",
    "FEDERATOR_PRODUCER",
    "MANAGEMENT_NODE_ACCESS"
  ]

  # Create roles under this client
  custom_roles = [
    for r in lookup(each.value, "roles", []) : {
      name = r
    }
  ]

  # Assign mapped roles from other clients to this client's service account
  service_account_role_ids = flatten([
    for m in lookup(each.value, "mapped_client_roles", []) : [
      for role_name in m.roles : {
        name        = role_name
        from_client = m.client
      }
    ]
  ])
}