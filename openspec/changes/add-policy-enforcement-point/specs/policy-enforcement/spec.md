## Purpose

Enforces attribute-based access policy on policy-aware Management Node APIs by delegating allow/deny decisions to an external Policy Decision Point (OPA), after existing authentication and certificate checks have passed.

## ADDED Requirements

### Requirement: Interception of policy-aware API requests
The system SHALL intercept every request to a configured policy-aware API path after authentication has completed, before the request reaches its handler.

#### Scenario: Request to a policy-aware endpoint is intercepted
- **WHEN** an authenticated request is made to a configured policy-aware path (e.g. `/api/v1/configuration/producer`)
- **THEN** the system evaluates the request against policy before invoking the endpoint handler

#### Scenario: Request to a non-policy-aware endpoint is not intercepted
- **WHEN** an authenticated request is made to a path that is not configured as policy-aware
- **THEN** the system does not perform policy enforcement and processes the request as before this change

### Requirement: Enrichment of policy decision requests
The system SHALL build a policy decision request containing the caller's identity, organisation, and the requested resource and action, derived from the authenticated security context and the incoming HTTP request.

#### Scenario: Decision request includes required attributes
- **WHEN** the system prepares to evaluate policy for an intercepted request
- **THEN** the decision request sent to the PDP includes the caller's client identity, organisation (where available), the target resource path, and the HTTP method as the action

#### Scenario: Missing identity attributes prevent evaluation
- **WHEN** an intercepted request has no resolvable client identity in the security context
- **THEN** the system rejects the request without invoking the PDP and returns an error response

### Requirement: PDP invocation and decision outcome
The system SHALL invoke the configured PDP (OPA) with the enriched decision request and interpret the response as either ALLOW or DENY.

#### Scenario: PDP returns an allow decision
- **WHEN** the PDP evaluates a decision request and returns an allow result
- **THEN** the system treats the outcome as ALLOW

#### Scenario: PDP returns a deny decision
- **WHEN** the PDP evaluates a decision request and returns a deny result (or any non-allow result)
- **THEN** the system treats the outcome as DENY

#### Scenario: PDP is unreachable or errors
- **WHEN** the PDP cannot be reached or returns an error response
- **THEN** the system treats the outcome as DENY and returns an error response to the caller without invoking the endpoint handler

### Requirement: Enforcement of the PDP decision
The system SHALL enforce the PDP's decision by allowing the request to proceed only on ALLOW, and rejecting it with an HTTP 403 response on DENY.

#### Scenario: Allowed request proceeds
- **WHEN** the PDP decision for an intercepted request is ALLOW
- **THEN** the request proceeds to its endpoint handler and receives its normal response

#### Scenario: Denied request is rejected
- **WHEN** the PDP decision for an intercepted request is DENY
- **THEN** the system responds with HTTP 403 and does not invoke the endpoint handler

### Requirement: Coexistence with existing authentication and access control
The system SHALL perform policy enforcement only after existing authentication and certificate validation mechanisms have already accepted the request, without altering their behavior.

#### Scenario: Unauthenticated request is rejected before policy evaluation
- **WHEN** a request without valid authentication is made to a policy-aware endpoint
- **THEN** the request is rejected by existing authentication mechanisms and no PDP invocation occurs

#### Scenario: Invalid certificate request is rejected before policy evaluation
- **WHEN** an authenticated request fails existing certificate validation
- **THEN** the request is rejected by certificate validation and no PDP invocation occurs

### Requirement: Audit logging of policy decisions
The system SHALL log each policy decision outcome, including the caller's client identity, the requested resource and action, the decision (ALLOW/DENY), and a correlation identifier.

#### Scenario: Allow decision is logged
- **WHEN** the PDP returns an ALLOW decision for a request
- **THEN** the system logs the client identity, resource, action, decision, and correlation identifier at INFO level

#### Scenario: Deny decision is logged
- **WHEN** the PDP returns a DENY decision (including PDP errors treated as DENY) for a request
- **THEN** the system logs the client identity, resource, action, decision, and correlation identifier at WARN level
