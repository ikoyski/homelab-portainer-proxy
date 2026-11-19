# Project Status and Handoff

> **Purpose:** Canonical continuity record for maintainers and AI coding assistants.
>
> **Last updated:** 2026-11-19
> **Repository:** `ikoyski/homelab-portainer-proxy`
> **Current focus:** Release 2 — implement the Portainer upstream adapter

## Current state

| Area | Status | Notes |
|---|---|---|
| Codebase skeleton | Created | Java 21, Spring Boot 4, Config Client and Actuator. |
| Caller authentication | Merged to `master` | Shared bearer filter, minimum 32 UTF-8 bytes, constant-time comparison and fail-closed startup validation. See ADR-0002. |
| Authentication unit tests | Added | Focused token tests are present. |
| Spring context smoke test | Follow-up merged | PR #3 supplies a test-only token fixture. CI/SonarCloud verification must still be checked independently. |
| Inbound API contract and request guard | Present on `master` | Fixed GET-only route catalog, path/method/query/body rejection and proxy-owned DTO definitions. No controllers yet. |
| Portainer upstream adapter | Not implemented | No endpoint discovery, Portainer API client, allowlist resolution, Docker inventory handlers or DTO mapping yet. |
| Portainer TLS trust decision | Accepted | Self-signed Portainer certificate must be trusted via a narrowly scoped runtime-mounted truststore; certificate and hostname/IP verification remain enabled. See ADR-0003. |
| Runtime port | Corrected in this branch | Application and Actuator share port 8081; Dockerfile and docs updated. Config Server runtime config already uses port 8081. |
| Architecture ADR-0001 | Proposed | Fixed-operation read-only architecture remains Proposed pending formal approval. |
| Caller-auth ADR-0002 | Accepted | Shared bearer authentication selected for the MCP-to-proxy hop. |
| Deployment | None | Nothing deployed; no proxy-to-Portainer connectivity verified. |

## Merged authentication implementation

- `SharedBearerTokenFilter` checks the Authorization bearer token before request handling.
- Missing, malformed or incorrect credentials receive HTTP 401 with a stable `UNAUTHORIZED` code.
- The expected secret must be configured and contain at least 32 UTF-8 bytes; startup fails closed otherwise.
- Token comparison uses `MessageDigest.isEqual`.
- Focused unit tests cover accepted, missing, malformed, incorrect and weakly configured tokens.
- PR #3 merged the test-only fixture to `master`; the fixture is not a production secret.
- Do not claim CI is green until the merged workflow result is verified. The earlier SonarCloud authorization failure was a separate scanner/organization authorization issue and did not establish whether the Maven tests passed.

## Settled design requirements

- The proxy returns only its own allowlisted DTOs; raw Portainer bodies are never forwarded.
- Portainer endpoint IDs are outbound-only and never caller-supplied.
- Configuration namespace for Portainer settings is `proxy.portainer.*`.
- Application and Actuator share the same listener on port 8081. No separate management listener.
- No Proxmox integration, MCP SDK/tools, Webtools OAuth resource server, central-user authorization, or environment-specific settings in this repo.
- Portainer TLS certificate and hostname/IP verification must remain enabled. The self-signed certificate is trusted only through a runtime-mounted truststore outside Git and the image.
- The proxy must stay on the private Docker network; bearer authentication does not replace network isolation.

## Remaining Release 2 work

1. Verify the merged authentication PR's CI/Maven test result independently of SonarCloud.
2. Implement the Portainer configuration properties and HTTP client with `X-API-Key`, bounded timeouts and response-size limits.
3. Implement Portainer endpoint discovery and server-side environment allowlist resolution.
4. Implement the fixed inbound GET-only API handlers.
5. Map upstream responses into proxy-owned DTOs only.
6. Add sanitized error handling and per-environment failure isolation.
7. Add tests for TLS trust and hostname mismatch, API-key header behavior, response bounds, allowlist enforcement and DTO filtering.
8. Integrate the MCP client and its own independent DTOs/tests.
9. Validate against the installed Portainer CE deployment.
10. Deploy only after explicit operator approval.

## Private runtime configuration

- Keep the shared bearer secret in private Config Server configuration for both services; never commit its value.
- Keep the Portainer API credential, base URL, environment mappings and truststore material only in private configuration/runtime.
- Do not put actual private addresses, environment names or secrets in this public repository.
- Secret-management cleanup is explicitly deferred; do not change the current private Config Server secret arrangement as part of this implementation increment.

## Starting a new engineering conversation

Read this file, `CLAUDE.md`, `README.md`, `docs/architecture/inbound-api-contract.md`, `docs/architecture/proxy-design.md`, then the ADRs. Also read the MCP repo's Release 2 document and ADRs. Verify code, CI and deployment state before claiming anything.
