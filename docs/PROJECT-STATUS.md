# Project Status and Handoff

> **Purpose:** Canonical continuity record for maintainers and AI coding assistants.
>
> **Last updated:** 2026-11-19
> **Repository:** `ikoyski/homelab-portainer-proxy`
> **Current focus:** Release 2 foundation — verify merged shared bearer authentication

## Current state

| Area | Status | Notes |
|---|---|---|
| Codebase skeleton | Created; initial local Maven build reported successful | That earlier result predates the current changes. |
| Caller authentication | Merged to `master`; CI verification fix in progress | Shared bearer filter, minimum 32 UTF-8 bytes, constant-time comparison and fail-closed startup validation. See ADR-0002. |
| Authentication unit tests | Added | Focused token tests are present. |
| Spring context smoke test | Fix in progress | Initial PR workflow failed because the test context did not supply the required test-only token. |
| GitHub Actions CI | Initial PR run failed; rerun pending | Failure cause identified and test fixture corrected on a follow-up branch. |
| Portainer proxy operations | Not implemented | No endpoint discovery, allowlist adapter, Docker inventory handlers, DTO mapping or upstream Portainer client yet. |
| Architecture ADR-0001 | Proposed | Fixed-operation read-only architecture remains Proposed pending formal approval. |
| Caller-auth ADR-0002 | Accepted | Shared bearer authentication selected for the MCP-to-proxy hop. |
| Deployment | None | Nothing deployed; no proxy-to-Portainer connectivity verified. |

## Merged authentication implementation

- `SharedBearerTokenFilter` checks the Authorization bearer token before request handling.
- Missing, malformed or incorrect credentials receive HTTP 401 with a stable `UNAUTHORIZED` code.
- The expected secret must be configured and contain at least 32 UTF-8 bytes; startup fails closed otherwise.
- Token comparison uses `MessageDigest.isEqual`.
- Focused unit tests cover accepted, missing, malformed, incorrect and weakly configured tokens.
- The initial CI run failed in `HomelabPortainerProxyApplicationTest.contextLoads`: the Spring test context did not supply `proxy.security.shared-token`. A test-only value is being added to `src/test/resources/application.yaml`; it is not a production secret.
- The real secret remains private Config Server configuration for both services. The proxy-side key is `proxy.security.shared-token`; the MCP-side key must be defined consistently in the MCP application.

## Settled design requirements

- The proxy returns only its own allowlisted DTOs; raw Portainer bodies are never forwarded.
- Portainer endpoint IDs are outbound-only and never caller-supplied.
- Configuration namespace for Portainer settings is `proxy.portainer.*`.
- Application port 8084; Actuator uses a separate internal management port that is never published.
- No Proxmox integration, MCP SDK/tools, Webtools OAuth resource server, central-user authorization, or environment-specific settings in this repo.
- Portainer TLS certificate and hostname verification must remain enabled.
- The proxy must stay on the private Docker network; bearer authentication does not replace network isolation.

## Remaining Release 2 work

1. Pass Maven tests and GitHub Actions CI for the authentication foundation.
2. Implement the fixed inbound GET-only API and strict request rejection behavior (the contract and initial request-surface guard are already present).
3. Implement Portainer endpoint discovery and server-side environment allowlist resolution.
4. Implement the Portainer upstream client with verified TLS, bounded timeouts and response limits.
5. Map upstream responses into proxy-owned DTOs only.
6. Add sanitized error handling and per-environment failure isolation.
7. Integrate the MCP client and its own independent DTOs/tests.
8. Validate against the installed Portainer CE 2.45.4 deployment.
9. Deploy only after explicit operator approval.

## Private runtime configuration

- Keep the shared bearer secret only in the private Config Server configuration for both services; never commit its value.
- Keep the Portainer API credential, base URL, environment mappings and truststore material only in the proxy's private configuration/runtime.
- Do not put actual private addresses, environment names or secrets in this public repository.

## Starting a new engineering conversation

Read this file, `CLAUDE.md`, `README.md`, `docs/architecture/inbound-api-contract.md`, `docs/architecture/proxy-design.md`, then the ADRs. Also read the MCP repo's Release 2 document and ADRs. Verify code, CI and deployment state before claiming anything.
