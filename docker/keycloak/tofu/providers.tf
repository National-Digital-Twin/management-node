provider "keycloak" {
  url            = var.keycloak_url
  realm          = var.keycloak_realm # manage realms from master
  client_id      = var.keycloak_client_id
  username       = var.keycloak_username
  password       = var.keycloak_password
  client_timeout = var.keycloak_client_timeout
}
