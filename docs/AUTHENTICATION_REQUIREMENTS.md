# Authentication Requirements

**Repository:** `management-node`  
**Description:** `Provides APIs to be accessed by Consumer and Producer Federators for the purpose of dynamic configuration management `  
**SPDX-License-Identifier:** `Apache-2.0 AND OGL-UK-3.0 `

---
Management Node uses OAuth 2.0 with JWT bearer tokens for authentication and authorization. Tokens are typically issued by Keycloak in this project’s reference setup.

Core requirements for every request to protected APIs:
- Bearer token: Requests must include `Authorization: Bearer <JWT>`.
- Audience (aud) claim: The token MUST contain an audience that includes `"management-node"`.
- resource_access claim: The token MUST include a `resource_access` claim, which carries client-application roles used for authorization decisions.

Notes:
- The API enforces role checks at endpoint level using Spring Security `@PreAuthorize` expressions.
- The Swagger UI documents the security scheme as HTTP bearer with JWT; you can use it to try endpoints by supplying a valid token.

## Token structure requirements

A compliant JWT will contain at least the following claims:
- `aud`: must include `management-node` (either as a string or within an array, depending on the issuer configuration).
- `resource_access`: an object mapping client IDs to role arrays.

Sample JWT payload (use this structure when testing locally):
```
{
  "exp": 1757863604,
  "iat": 1757861804,
  "jti": "trrtcc:a245819b-9a9f-648f-2e95-2390f6987c03",
  "iss": "https://localhost:8443/realms/mng-node",
  "aud": [
    "management-node",
    "FEDERATOR_HEG"
  ],
  "sub": "ec13a601-9b02-443a-99ff-66f1eb146ae9",
  "typ": "Bearer",
  "azp": "FEDERATOR_BCC",
  "resource_access": {
    "management-node": {
      "roles": [
        "access_producer_configurations",
        "access_consumer_configurations",
        "BrownfieldLandAvailability",
        "PendingPlanningApplications"
      ]
    }
  },
  "scope": "FEDERATOR_PRODUCER MANAGEMENT_NODE_ACCESS FEDERATOR_CONSUMER"
}
```

Notes:
- The aud claim may be a list (as shown) and must include "management-node".
- The resource_access.management-node.roles array must contain the role required for the API you are calling.

## Role requirements per API

- Producer API: Federator clients may access Producer configuration only when their token contains the role `access_producer_configurations` under the `resource_access` for the audience/client `management-node`.
  - Enforcement in code: `@PreAuthorize("hasRole('ROLE_management-node:access_producer_configurations')")` on `/api/v1/configuration/producer`.

- Consumer API: Federator clients may access Consumer configuration only when their token contains the role `access_consumer_configurations` under the `resource_access` for the audience/client `management-node`.
  - Enforcement in code: `@PreAuthorize("hasRole('ROLE_management-node:access_consumer_configurations')")` on `/api/v1/configuration/consumer`.

## How this maps to Keycloak

- In Keycloak, roles are typically assigned to a client (here conceptually the `management-node` client) and appear in tokens under `resource_access["management-node"].roles`.
- Ensure the token’s audience includes `management-node`. This can be achieved by:
  - Setting the client as an audience in the token via an Audience mapper, or
  - Using the `audience resolve`/`Full Scope Allowed` as per your realm design.
- Create and assign the following client roles on the `management-node` client:
  - `access_producer_configurations`
  - `access_consumer_configurations`
- Assign these roles to the appropriate Producer or Consumer Federator clients or service accounts.

## Requesting tokens (example)

Using client credentials with mTLS (as per the project’s Keycloak setup):
```
curl --location 'https://localhost:8443/realms/management-node/protocol/openid-connect/token' \
  --cert client.crt --key client.key \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'client_id=<federator-client-id>' \
  --data-urlencode 'grant_type=client_credentials'
```

Supply the returned access token to the Management Node API requests:
```
curl -k 'https://localhost:8090/api/v1/configuration/producer' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>'
```

## Summary

- Authentication: JWT bearer tokens.
- Mandatory claims: `aud` includes `management-node`, and `resource_access` present.
- Authorization:
  - Producer API requires role: `access_producer_configurations`.
  - Consumer API requires role: `access_consumer_configurations`.
- Swagger/OpenAPI: Use Swagger UI at `/swagger-ui.html` to explore and test with a valid token.