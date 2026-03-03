path "pki-client/data/node-net/client/*" {
  capabilities = ["create", "read", "update", "delete", "list"]
}

path "pki-client/metadata/node-net/client/*" {
  capabilities = ["read", "list"]
}

path "sys/mounts" {
  capabilities = ["read"]
}