## Why

Management Node currently authenticates and certificate-validates requests but has no mechanism to enforce fine-grained, attribute-based access policy before a request reaches its handler. DPAV-3017 requires a Policy Enforcement Point (PEP) that intercepts policy-aware API requests, enriches them with identity/organisation/resource attributes, and delegates the allow/deny decision to an external Policy Decision Point (OPA), so access control decisions are made consistently and can evolve independently of application code.

## What Changes

- Add a `PolicyEnforcementInterceptor` that intercepts requests to policy-aware APIs, after authentication has already run.
- Add a `PolicyDecisionClient` that builds a decision request (identity, organisation, resource, action attributes) and calls OPA's REST API via Spring's `RestClient`.
- Enforce the PDP decision: allow the request to proceed on `ALLOW`, short-circuit with HTTP 403 on `DENY`.
- Wire the interceptor to `/api/v1/configuration/**` (the only policy-aware endpoints currently exposed by this service: producer and consumer configuration lookups). No other controllers are affected.
- Add configuration properties for the OPA endpoint (URL, decision path, timeout) under `application.yml`, following existing `application.*` property conventions.
- Log each PEP decision (client id, resource, action, decision, correlation id) at INFO (ALLOW) / WARN (DENY) for audit/troubleshooting (covers AC6).
- Existing authentication (`SecurityConfig`, `KeycloakJwtAuthenticationConverter`) and certificate validation (`CertificateValidationInterceptor`) are unchanged; the PEP runs as an additional, later interceptor stage.

## Capabilities

### New Capabilities
- `policy-enforcement`: Interception of policy-aware API requests, enrichment with policy attributes, PDP invocation, and enforcement of the allow/deny decision.

### Modified Capabilities
(none — existing authentication/certificate-validation capabilities are not changing behavior; the PEP is additive.)

## Impact

- **New code**: `config/PolicyEnforcementInterceptor`, a `service/providers/policy` package (`PolicyDecisionClient`, request/response DTOs), configuration properties class.
- **Modified code**: `WebConfig` (register new interceptor with path patterns), `application.yml` (new `application.opa.*` properties), `pom.xml` (no new dependency required — `RestClient` ships with `spring-boot-starter-web`, already present).
- **APIs affected**: `/api/v1/configuration/producer`, `/api/v1/configuration/consumer` now require an ALLOW decision from OPA in addition to existing JWT authentication.
- **External dependency**: requires an OPA instance reachable at the configured URL; no OPA policies are authored as part of this change (assumed to exist/be provided separately) — decision contract only.
- **Tests**: unit tests for interceptor and decision client, integration tests (`@SpringBootTest` / `MockMvc`) covering permitted and denied requests through `/api/v1/configuration/**`.
