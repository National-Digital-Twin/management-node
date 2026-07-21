**Repository:** `management-node`  
**Description:** `The Management Node Module is a Spring Boot application that provides APIs to be accessed by Consumer and Producer Federators. It implements a secure communication architecture using Mutual TLS (MTLS) connectivity between Federator instances and itself, as well as establishing zero trust connectivity with Keycloak for authentication and authorization.`  
**SPDX-License-Identifier:** `Apache-2.0 AND OGL-UK-3.0 `

# Overview

This directory contains OpenTofu code to provision and manage Keycloak resources for the Management Node:
- Realm definition and settings
- Application clients and roles
- Client scopes and role mappings
- Optional federator clients via a module (modules/federator_client)

The configuration uses the Keycloak provider and an S3 backend for state (configured via backend tfvars).

Important: Backend selection
- Local runs: set a local backend.
- AWS runs: use the S3 backend as shown in backend.tf.

Examples:

Local backend (for running on your machine):
```hcl
terraform {
  required_version = ">= 1.6.0"
  required_providers {
    keycloak = {
      source  = "keycloak/keycloak"
      version = "~> 5.4"
    }
  }
  backend "local" {}
}
```

AWS S3 backend (for running in AWS):
```hcl
terraform {
  required_version = ">= 1.6.0"
  required_providers {
    keycloak = {
      source  = "keycloak/keycloak"
      version = "~> 5.4"
    }
  }
  backend "s3" {}
}
```

---

## Directory Layout

- backend.tf           - Declares the S3 backend and required providers
- providers.tf         - Configures the Keycloak provider using variables
- variables.tf         - Input variables used across the configuration
- realm.tf             - Realm creation and base configuration
- clients.tf           - Clients and role definitions
- client_scopes.tf     - Client scopes and mappers
- terraform.tfvars     - Default variable values for local/dev usage
- backends/            - Backend config files (e.g., dev-backend.tfvars)
- tfvars/              - Optional per-workspace tfvars files (e.g., dev.tfvars)
- modules/             - Reusable modules (e.g., federator_client)
- Makefile             - Helper targets to init/plan/apply/destroy/validate

Tip: The Makefile expects to run commands from this tofu folder and supports a WORKSPACE and DIR variable. For this repository, DIR should be set to the current directory (.).

---

## Using the Makefile

The Makefile simplifies running OpenTofu commands.

Defaults:
- WORKSPACE=dev
- DIR=01-global (upstream default; override to . for this repo)

Recommended to always pass DIR=.

### 1. Setup (`make init`)
Initializes OpenTofu, selects/creates the workspace, and configures the S3 backend.

```sh
# From docker/keycloak/tofu
make init WORKSPACE=dev DIR=.
```

If your backend file is named differently, adjust accordingly or rename it to match backends/<workspace>-backend.tfvars. For example, ensure backends/dev-backend.tfvars exists for WORKSPACE=dev.

### 2. Plan changes (`make plan`)
Generates an execution plan. terraform.tfvars is loaded automatically; tfvars/dev.tfvars can be used for per-workspace overrides.

```sh
make plan WORKSPACE=dev DIR=.
```

### 3. Apply changes (`make apply`)
Applies the previously generated plan.

```sh
make apply WORKSPACE=dev DIR=.
```

Alternatively, apply directly with auto-approve:

```sh
make apply-auto-approve WORKSPACE=dev DIR=.
```

### 4. Destroy resources (`make destroy`)
Plans a destroy and destroys resources for the given workspace.

```sh
make destroy-plan WORKSPACE=dev DIR=.
make destroy WORKSPACE=dev DIR=.
```

### 5. Validate and format (`make validate`, `make format`)

```sh
make format
make validate WORKSPACE=dev DIR=.
```

### 6. Pre-check (`make pre-check`)
Runs fmt -check and validate.

```sh
make pre-check WORKSPACE=dev DIR=.
```

---

## Variables
Key variables you may need to set (see variables.tf and terraform.tfvars):
- keycloak_url, keycloak_realm, keycloak_client_id, keycloak_username, keycloak_password, keycloak_client_timeout
- management_realm_name
- client_access_token_lifespan_seconds
- federator_clients (structured list for module-driven client creation and role mappings)

These can be provided via terraform.tfvars, tfvars/<workspace>.tfvars, or -var/-var-file flags.

---

## Example Workflow

```sh
cd docker/keycloak/tofu
make init WORKSPACE=dev DIR=.
make format
make validate WORKSPACE=dev DIR=.
make plan WORKSPACE=dev DIR=.
make apply WORKSPACE=dev DIR=.
```

---

## Using a local backend (no S3) on your machine
If you want to try this locally without configuring an S3 bucket, you can switch the backend from S3 to local. There are two simple approaches:

### Option A: Use a local backend block
Edit docker/keycloak/tofu/backend.tf and change the backend block to local:

```hcl
terraform {
  required_version = ">= 1.6.0"
  required_providers {
    keycloak = {
      source  = "keycloak/keycloak"
      version = "~> 5.4"
    }
  }
  backend "local" {
    # The default path is ./terraform.tfstate
    path = "terraform.tfstate"
  }
}
```

Then use the Makefile targets (recommended):

```sh
cd docker/keycloak/tofu
# Initialize with local backend (no -backend-config needed)
make init WORKSPACE=dev DIR=.
# Plan and apply using Makefile targets
make plan WORKSPACE=dev DIR=.
make apply WORKSPACE=dev DIR=.
```

### Option B: Comment out the S3 backend
Alternatively, simply comment out the S3 backend line in backend.tf so there is no backend block. OpenTofu defaults to the local backend in this case:

Before:
```hcl
terraform {
  required_version = ">= 1.6.0"
  required_providers {
    keycloak = {
      source  = "keycloak/keycloak"
      version = "~> 5.4"
    }
  }
  backend "s3" {}
}
```

After (S3 backend commented out):
```hcl
terraform {
  required_version = ">= 1.6.0"
  required_providers {
    keycloak = {
      source  = "keycloak/keycloak"
      version = "~> 5.4"
    }
  }
  # backend "s3" {}
}
```

Then initialize and apply as in Option A using the Makefile:

```sh
cd docker/keycloak/tofu
make init WORKSPACE=dev DIR=.
make plan WORKSPACE=dev DIR=.
make apply WORKSPACE=dev DIR=.
```

Notes for local usage:
- The state file terraform.tfstate will be created next to backend.tf (and is already ignored by .gitignore).
- Use the Makefile for all operations; after switching to local backend in backend.tf, simply run:
  ```sh
  cd docker/keycloak/tofu
  make init WORKSPACE=dev DIR=.
  make plan WORKSPACE=dev DIR=.
  make apply WORKSPACE=dev DIR=.
  ```
- To switch back to S3 later, restore the backend "s3" {} block and run:
  ```sh
  cd docker/keycloak/tofu
  make init WORKSPACE=dev DIR=.
  ```

---

## Notes
- Backend: The backend is defined as S3 in backend.tf and is configured via backends/<workspace>-backend.tfvars. Update bucket/key/region to match your environment.
- Provider auth: providers.tf uses admin credentials (admin-cli) by default for local dev. For production, configure a service account and secure secrets appropriately.
- Docker: When using docker-compose Keycloak locally (default at http://localhost:8080), the sample terraform.tfvars should work once admin credentials are set to admin/password.

---

## Contributors
Thanks to all contributors of this repository: https://github.com/National-Node-Net/management-node/graphs/contributors
