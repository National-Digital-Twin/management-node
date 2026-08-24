## Context

Management Node is a Spring Boot 3.5 / Spring Security 6.5 service. Requests already pass through JWT authentication (`SecurityConfig` + `KeycloakJwtAuthenticationConverter`) and, for `/api/**` (excluding `/api/v1/certificate/**`), a `CertificateValidationInterceptor` registered via `WebMvcConfigurer#addInterceptors`. That interceptor is the established pattern for a request-level gate: read `SecurityContextHolder`, validate, short-circuit with a JSON `ErrorResponse` and non-2xx status on rejection. See proposal.md - Why for the business motivation. Scope of protected endpoints and OPA transport were decided with the user: `/api/v1/configuration/**` only, and Spring's `RestClient`.

## Goals / Non-Goals

**Goals:**
- Add a PEP interceptor stage that runs after authentication (and, where applicable, after certificate validation) and before the handler.
- Keep the decision contract with OPA simple enough for an MVP: single synchronous call, boolean-shaped result, no policy bundle management in this repo.
- Make the set of protected paths and the OPA endpoint configurable without code changes.

**Non-Goals:**
- Authoring or shipping actual Rego policies for OPA — out of scope; this change only defines and calls the decision contract.
- Caching or batching PDP decisions — MVP does one synchronous call per intercepted request.
- Protecting `/api/v1/certificate/**` — certificate endpoints are bootstrap/identity-establishing and stay on existing cert-based checks only, consistent with `WebConfig`'s existing exclusion.
- Async/reactive PDP calls — the app is fully synchronous (`spring-boot-starter-web`, MVC), so `RestClient` (blocking) is used, not `WebClient`.

## Decisions

### 1. HandlerInterceptor, not a Filter
Follow the existing `CertificateValidationInterceptor` pattern rather than a `jakarta.servlet.Filter`. Rationale: interceptors run after Spring Security's filter chain has populated `SecurityContextHolder`, and the codebase already has one interceptor + a `WebConfig` registration point with path include/exclude patterns — reusing it keeps the request-processing pipeline in one mental model instead of introducing a second cross-cutting mechanism.

Registration in `WebConfig`: add path patterns for `/api/v1/configuration/**` only (per user decision), ordered after `CertificateValidationInterceptor` (interceptors run in registration order within `InterceptorRegistry`).

### 2. Spring `RestClient` for PDP calls
`spring-boot-starter-web` already provides `RestClient` (Spring Boot 3.5). No new Maven dependency needed (rejected `WebClient`, which would pull in `reactor-core`/`spring-boot-starter-webflux` for a single blocking call in an otherwise synchronous app; rejected raw `RestTemplate` since `RestClient` is the current, non-deprecated equivalent). One `RestClient` bean configured with base URL, connect/read timeouts from `application.opa.*` properties.

### 3. Decision request/response contract
OPA's standard REST API is `POST {opa-url}/v1/data/{policy-path}` with a JSON body `{"input": {...}}`, returning `{"result": ...}`. This change defines:
- Request: `{"input": {"clientId": "...", "organisation": "...", "resource": "<request URI>", "action": "<HTTP method>"}}`
- Response: `{"result": true|false}` — `true` → ALLOW, anything else (including `false`, missing `result`, or non-2xx/network error) → DENY.

This mirrors OPA's documented decision API rather than inventing a bespoke one, so any standard OPA deployment works without a custom plugin.

### 4. Client identity / organisation source
Reuse `EnhancedPrincipal` (already populated by `KeycloakJwtAuthenticationConverter` and consumed by `CertificateValidationInterceptor`) for `clientId`. Organisation attribute: read from `EnhancedPrincipal` if it already exposes one; otherwise (to be confirmed at implementation time by inspecting `EnhancedPrincipal`/`CustomJwtAuthenticationToken`) omit it from the request rather than adding new claim-parsing logic — enrichment only uses attributes already available on the authenticated principal, consistent with "integrate without altering existing authentication behaviour."

### 5. Failure handling = fail closed
Any PDP error (timeout, connection failure, non-2xx, malformed body) is treated as DENY (HTTP 403), not fail-open. Rationale: this is an access-control gate; failing open would silently disable enforcement during an OPA outage.

### 6. Logging
Use the existing `Slf4j` + MDC pattern (`ClientIdMdcFilter` already puts `clientId` in MDC, visible in the configured log pattern). PEP adds one log line per decision (INFO for ALLOW, WARN for DENY) with resource, action, decision, and a per-request correlation id (reuse `ErrorResponse`'s existing `UUID.randomUUID()` pattern used for error responses elsewhere in the codebase).

## Risks / Trade-offs

- [OPA outage causes total denial of configuration endpoints] → Mitigated by MVP scope being limited to `/api/v1/configuration/**` (not certificate/bootstrap flows, which stay available); documented as an operational dependency in proposal.md - Impact.
- [Organisation attribute may not exist on `EnhancedPrincipal` yet] → Implementation confirms this against current `EnhancedPrincipal`/JWT claims; if absent, decision requests omit it rather than blocking the change (see Decision 4). Policy authors are informed via this design that `organisation` may be absent.
- [No Rego policy shipped with this change] → OPA must be configured with a compatible policy for `/api/v1/configuration/**` before this reaches an environment where OPA is enforced; local/dev config should point at a permissive or stubbed OPA to avoid blocking other development.
- [Synchronous PDP call adds latency to every configuration request] → Bounded by a short configurable timeout (`application.opa.timeout`); on timeout, fail closed (Decision 5) rather than hang.
