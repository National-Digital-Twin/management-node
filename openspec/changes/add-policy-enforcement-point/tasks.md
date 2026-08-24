## 1. Configuration properties

- [x] 1.1 Add `application.opa.*` properties (`url`, `decision-path`, `connect-timeout`, `read-timeout`) to `application.yml` with sane local defaults, and a `@ConfigurationProperties`-backed (or `@Value`-backed) properties class under `config/`; verify the application context loads with the new properties bound (existing `SecurityConfigTest`/`WebConfigTest`-style context test passes).

## 2. PDP decision client

- [x] 2.1 Add `PolicyDecisionRequest`/`PolicyDecisionResponse` DTOs (clientId, organisation, resource, action / result) under `service/providers/policy`; verify with a serialization unit test (Jackson round-trip matches OPA's `{"input": {...}}` / `{"result": ...}` shape from design.md - Decision 3).
- [x] 2.2 Add a `RestClient` bean configured from the properties in 1.1; verify with a context test asserting the bean is created with the configured base URL/timeouts.
- [x] 2.3 Implement `PolicyDecisionClient` that POSTs a `PolicyDecisionRequest` to `{opa.url}{opa.decision-path}` and maps the response to an ALLOW/DENY enum, treating non-2xx, network errors, and timeouts as DENY (design.md - Decision 5); verify with unit tests using a mocked `RestClient` covering: allow response, deny response, malformed body, HTTP error, connection failure/timeout.

## 3. Policy Enforcement interceptor

- [x] 3.1 Implement `PolicyEnforcementInterceptor` (`HandlerInterceptor`) that reads `clientId`/organisation from `EnhancedPrincipal` in `SecurityContextHolder`, builds a `PolicyDecisionRequest` (resource = request URI, action = HTTP method), calls `PolicyDecisionClient`, and on ALLOW returns true / on DENY writes an `ErrorResponse` with HTTP 403 and returns false, mirroring `CertificateValidationInterceptor`'s `writeError` pattern; verify with unit tests covering: missing clientId (rejected without calling PDP), ALLOW (returns true), DENY (403 + no handler invocation).
- [x] 3.2 Add decision audit logging (INFO on ALLOW, WARN on DENY, including clientId, resource, action, decision, correlation id) per design.md - Decision 6; verify with a unit test asserting a log line is emitted for each outcome (e.g. via a test log appender).

## 4. Wiring

- [x] 4.1 Register `PolicyEnforcementInterceptor` in `WebConfig` for `/api/v1/configuration/**` only, ordered after `CertificateValidationInterceptor`; verify with a `WebConfigTest`-style test asserting the interceptor is registered for the expected path pattern and not for `/api/v1/certificate/**`.

## 5. Integration tests

- [ ] 5.1 Add `MockMvc`/`@SpringBootTest` integration tests against `/api/v1/configuration/producer` and `/api/v1/configuration/consumer` covering: authenticated request + PDP ALLOW returns normal response; authenticated request + PDP DENY returns 403; unauthenticated request is rejected before any PDP call (mock/stub the PDP client or its `RestClient` dependency); verify all new integration tests pass.

## 6. Spec sync and full verification

- [ ] 6.1 Run the full test suite (`mvn test` or project's configured command) and confirm all existing and new tests pass with no regressions.
- [ ] 6.2 Run `openspec validate add-policy-enforcement-point --strict` and confirm it passes before archiving.
