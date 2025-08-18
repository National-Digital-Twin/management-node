output "client_uuid" {
  description = "UUID of the created Keycloak client (container client_id for role assignments)"
  value       = keycloak_openid_client.this.id
}

output "custom_role_names" {
  description = "List of custom role names created under this client (if any)"
  value       = keys(keycloak_role.custom_roles)
}