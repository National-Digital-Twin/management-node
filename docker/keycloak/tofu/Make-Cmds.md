# Keycloak OpenTofu Make Commands

This document describes how to use the Makefile in this folder to manage Keycloak resources (realm, clients, client scopes) with OpenTofu.

Important notes:
- Run these commands from: docker/keycloak/tofu
- Use DIR=. for this repository (the Makefile default DIR=01-global is an upstream default).
- Workspaces (e.g., dev) map to different state and tfvars files.

---

## Quick start

Initialize OpenTofu, select/create the workspace, and configure the S3 backend:

```sh
make init WORKSPACE=dev DIR=.
```

If your backend file is custom, make sure it matches backends/<workspace>-backend.tfvars. Example for dev: backends/dev-backend.tfvars.

---

## Plan and apply

Plan changes (loads terraform.tfvars automatically and tfvars/<workspace>.tfvars if present):

```sh
make plan WORKSPACE=dev DIR=.
```

Apply the last plan:

```sh
make apply WORKSPACE=dev DIR=.
```

Or apply directly with auto-approve:

```sh
make apply-auto-approve WORKSPACE=dev DIR=.
```

---

## Destroy

Create a destroy plan and destroy resources for the selected workspace:

```sh
make destroy-plan WORKSPACE=dev DIR=.
make destroy WORKSPACE=dev DIR=.
```

---

## Validation and formatting

Format all files and validate configuration:

```sh
make format
make validate WORKSPACE=dev DIR=.
```

Pre-check (fmt -check + validate):

```sh
make pre-check WORKSPACE=dev DIR=.
```

Pre-commit convenience target (runs format and validate):

```sh
make pre-commit WORKSPACE=dev DIR=.
```

---

## Upgrade and re-init

If providers/modules were updated or you need a clean init:

```sh
make init-upgrade WORKSPACE=dev DIR=.
```

---

## Variables and files

- Backend config: backends/<workspace>-backend.tfvars (e.g., backends/dev-backend.tfvars)
- Per-workspace variables: tfvars/<workspace>.tfvars (e.g., tfvars/dev.tfvars)
- Default variables: terraform.tfvars

Key variables (see variables.tf and terraform.tfvars):
- keycloak_url, keycloak_realm, keycloak_client_id, keycloak_username, keycloak_password, keycloak_client_timeout
- management_realm_name, client_access_token_lifespan_seconds
- federator_clients (module input for optional federator clients and role mappings)

---

## Example workflow

```sh
# From docker/keycloak/tofu
make init WORKSPACE=dev DIR=.
make plan WORKSPACE=dev DIR=.
make apply WORKSPACE=dev DIR=.
```

That's it - no AWS regional/global directories are needed here. This Makefile and commands are scoped to the Keycloak OpenTofu configuration in this folder.


