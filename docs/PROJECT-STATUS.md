# Project Status and Handoff

> **Purpose:** Canonical continuity record for maintainers and AI coding assistants.
>
> **Last updated:** 2026-11-19
> **Repository:** ikoyski/homelab-portainer-proxy
> **Current focus:** Release 2 — endpoint discovery, allowlist resolution and inventory mapping

## Current state

| Area | Status | Notes |
|---|---|---|
| Codebase skeleton | Created | Java 21, Spring Boot 4, Config Client and Actuator. |
| Caller authentication | Merged to master | Shared bearer filter, minimum 32 UTF-8 bytes, constant-time comparison and fail-closed startup validation. See ADR-0002. |
| Authentication unit tests | Added | Focused token tests are present. |
| Spring context smoke test | Follow-up merged | PR #3 supplies a test-only token fixture. CI/SonarCloud verification must still be checked independently. |
| Inbound API contract and request guard | Present on master | Fixed GET-only route catalog, path/method/query/body rejection and proxy-owned DTO definitions. No controllers yet. |
| Portainer configuration and HTTP client | Implemented on this feature branch | HTTPS-only base URL, X-API-Key, fixed GET methods, bounded connect/read timeouts and streaming response-size limit. Tests added; CI not yet run for this branch. |
| Endpoint discovery and allowlist resolution | Not implemented | Need to map discovered Portainer endpoints to the private configured host-key allowlist. |
| Inventory handlers and DTO mapping | Not implemented | No controllers or mapping from upstream JSON into proxy-owned response DTOs yet. |
| Portainer TLS trust decision | Accepted | Self-signed Portainer certificate must be trusted via a narrowly scoped runtime-mounted truststore; certificate and hostname/IP verification remain enabled. See ADR-0003. |
| Runtime port | Corrected in this branch | Application and Actuator share port 8081; Dockerfile and docs updated. Config Server runtime config already uses port 8081. |
| Architecture ADR-0001 | Proposed | Fixed-operation read-only architecture remains Proposed pending formal approval. |
| Caller-auth ADR-0002 | Accepted | Shared bearer authentication selected for the MCP-to-proxy hop. |
| Deployment | None | Nothing deployed; no proxy-to-Portainer connectivity verified. |

## Implemented upstream client foundation

- PortainerProperties binds proxy.portainer.* from Config Server and rejects non-HTTPS URLs, missing API keys, invalid timeouts/limits and invalid or duplicate host keys.
- PortainerClientConfiguration creates a JDK HTTP client using the JVM default SSL context. A runtime-mounted JVM truststore therefore supplies the self-signed Portainer trust anchor without bypassing TLS verification.
- PortainerApiClient exposes only fixed GET operations: endpoint listing, Docker info, container listing (all=true) and network listing.
- The API key is sent as X-API-Key; the caller's bearer token is not forwarded upstream.
- ResponseSizeLimitInterceptor rejects responses with an oversized declared Content-Length and also caps streamed bodies without relying on Content-Length.
- Portainer endpoint IDs are accepted only as internal positive integer arguments. Endpoint discovery and mapping from configured host keys are not yet implemented.
- Automated tests were added for configuration validation, fixed upstream paths/header behavior, invalid endpoint IDs and response-size enforcement. They have not yet been executed in CI or locally in this session.

## Merged authentication implementation

- SharedBearerTokenFilter checks the Authorization bearer token before request handling.
- Missing, malformed or incorrect credentials receive HTTP 401 with a stable UNAUTHORIZED code.
- The expected secret must be configured and contain at least 32 UTF-8 bytes; startup fails closed otherwise.
- Token comparison uses MessageDigest.isEqual.
- Focused unit tests cover accepted, missing, malformed, incorrect and weakly configured tokens.
- PR #3 merged the test-only fixture to master; the fixture is not a production secret.
- Do not claim CI is green until the merged workflow result is verified. The earlier SonarCloud authorization failure was a separate scanner/organization authorization issue and did not establish whether the Maven tests passed.

## Settled design requirements

- The proxy returns only its own allowlisted DTOs; raw Portainer bodies are never forwarded.
- Portainer endpoint IDs are outbound-only and never caller-supplied.
- Configuration namespace for Portainer settings is proxy.portainer.*.
- Application and Actuator share the same listener on port 8081. No separate management listener.
- No Proxmox integration, MCP SDK/tools, Webtools OAuth resource server, central-user authorization, or environment-specific settings in this repo.
- Portainer TLS certificate and hostname/IP verification must remain enabled. The self-signed certificate is trusted only through a runtime-mounted truststore outside Git and the image.
- The proxy must stay on the private Docker network; bearer authentication does not replace network isolation.

## Remaining Release 2 work

1. Verify the merged authentication PR's CI/Maven test result independently of SonarCloud.
2. Implement Portainer endpoint discovery and server-side environment allowlist resolution.
3. Implement the fixed inbound GET-only API handlers.
4. Map upstream responses into proxy-owned DTOs only.
5. Add sanitized error handling and per-environment failure isolation.
6. Add TLS integration tests for trust and hostname mismatch, plus tests for allowlist enforcement and DTO filtering.
7. Integrate the MCP client and its own independent DTOs/tests.
8. Validate against the installed Portainer CE deployment.
9. Deploy only after explicit operator approval.

## Private runtime configuration

- Keep the shared bearer secret in private Config Server configuration for both services; never commit its value.
- Keep the Portainer API credential, base URL, environment mappings and truststore material only in private configuration/runtime.
- Do not put actual private addresses, environment names or secrets in this public repository.
- Secret-management cleanup is explicitly deferred; do not change the current private Config Server secret arrangement as part of this implementation increment.

## Starting a new engineering conversation

Read this file, CLAUDE.md, README.md, inbound-api-contract.md, proxy-design.md and the ADRs. Also read the MCP repo's Release 2 document and ADRs. Verify code, CI and deployment state before claiming anything.
