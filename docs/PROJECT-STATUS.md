# Project Status and Handoff

> **Purpose:** Canonical continuity record for maintainers and AI coding assistants.
>
> **Last updated:** 2026-11-19
> **Repository:** ikoyski/homelab-portainer-proxy
> **Current focus:** Release 2 — endpoint discovery, allowlist resolution and inventory mapping

## Current state

| Area | Status | Notes |
|---|---|---|
| Codebase skeleton | Merged | Java 21, Spring Boot 4, Config Client and Actuator. |
| Caller authentication | Merged | Shared bearer filter, minimum 32 UTF-8 bytes, constant-time comparison and fail-closed startup validation. See ADR-0002. |
| Authentication unit tests | Added | Focused token tests are present. |
| Spring context smoke test | Merged | PR #3 supplies a test-only token fixture. |
| Inbound API contract and request guard | Merged | Fixed GET-only route catalog, path/method/query/body rejection and proxy-owned DTO definitions. No controllers yet. |
| Portainer configuration and HTTP client | Merged | HTTPS-only base URL, X-API-Key, fixed GET methods, bounded connect/read timeouts and streaming response-size limit. |
| Portainer endpoint discovery and allowlist resolution | Implemented on current feature branch | Fresh endpoint discovery; exact endpoint-name match against configured host keys; missing, duplicate and invalid-ID outcomes are distinct. Endpoint IDs remain internal. |
| Inventory handlers and DTO mapping | Not implemented | No controllers or mapping from upstream JSON into proxy-owned response DTOs yet. |
| Portainer TLS trust decision | Accepted | Self-signed Portainer certificate must be trusted via a narrowly scoped runtime-mounted truststore; certificate and hostname/IP verification remain enabled. See ADR-0003. |
| Runtime port | Merged | Application and Actuator share port 8081; Dockerfile and docs updated. |
| Architecture ADR-0001 | Proposed | Fixed-operation read-only architecture remains Proposed pending formal approval. |
| Caller-auth ADR-0002 | Accepted | Shared bearer authentication selected for the MCP-to-proxy hop. |
| Deployment | None | Nothing deployed; no proxy-to-Portainer connectivity verified. |

## Implemented upstream client foundation

- PortainerProperties binds proxy.portainer.* from Config Server and rejects non-HTTPS URLs, missing API keys, invalid timeouts/limits and invalid or duplicate host keys.
- PortainerClientConfiguration creates a JDK HTTP client using the JVM default SSL context. A runtime-mounted JVM truststore therefore supplies the self-signed Portainer trust anchor without bypassing TLS verification.
- PortainerApiClient exposes only fixed GET operations: endpoint listing, Docker info, container listing (all=true) and network listing.
- The API key is sent as X-API-Key; the caller's bearer token is not forwarded upstream.
- ResponseSizeLimitInterceptor rejects responses with an oversized declared Content-Length and also caps streamed bodies without relying on Content-Length.
- PortainerEndpointResolver performs fresh endpoint discovery and matches the Portainer endpoint Name exactly against the configured proxy host-key allowlist.
- Resolver outcomes distinguish RESOLVED, NOT_FOUND, AMBIGUOUS and INVALID_ENDPOINT_ID. It never chooses arbitrarily between duplicate names and never includes unconfigured endpoints in its result.
- Endpoint IDs exist only in the internal resolver result for subsequent adapter calls; they are not inbound parameters or response DTO fields.
- Automated tests cover exact matching, exclusion of unconfigured names, duplicate names, invalid IDs and malformed discovery responses. They have been added on the current feature branch but have not yet been run in CI or locally in this session.

## Merged authentication and upstream client implementation

- SharedBearerTokenFilter checks the Authorization bearer token before request handling.
- Missing, malformed or incorrect credentials receive HTTP 401 with a stable UNAUTHORIZED code.
- The expected secret must be configured and contain at least 32 UTF-8 bytes; startup fails closed otherwise.
- Token comparison uses MessageDigest.isEqual.
- PR #3 merged the test-only fixture to master; the fixture is not a production secret.
- PR #4 merged the Portainer HTTPS client foundation, response limits, TLS truststore guidance and shared port 8081 configuration. The operator reports the merged build completed successfully.
- Do not claim GitHub Actions CI is green until its workflow result is independently verified.

## Settled design requirements

- The proxy returns only its own allowlisted DTOs; raw Portainer bodies are never forwarded.
- Portainer endpoint IDs are outbound-only and never caller-supplied.
- Endpoint resolution is exact-name based against the private configured allowlist; no hardcoded endpoint IDs or environment mappings belong in this public repository.
- Configuration namespace for Portainer settings is proxy.portainer.*.
- Application and Actuator share the same listener on port 8081. No separate management listener.
- No Proxmox integration, MCP SDK/tools, Webtools OAuth resource server, central-user authorization, or environment-specific settings in this repo.
- Portainer TLS certificate and hostname/IP verification must remain enabled. The self-signed certificate is trusted only through a runtime-mounted truststore outside Git and the image.
- The proxy must stay on the private Docker network; bearer authentication does not replace network isolation.

## Remaining Release 2 work

1. Verify the merged PR workflows and run Maven verification for the current branch.
2. Implement the fixed inbound GET-only API handlers.
3. Map upstream responses into proxy-owned DTOs only.
4. Add sanitized error handling and per-environment failure isolation.
5. Add TLS integration tests for trust and hostname mismatch, plus tests for allowlist enforcement and DTO filtering.
6. Integrate the MCP client and its own independent DTOs/tests.
7. Validate against the installed Portainer CE deployment.
8. Deploy only after explicit operator approval.

## Private runtime configuration

- Keep the shared bearer secret in private Config Server configuration for both services; never commit its value.
- Keep the Portainer API credential, base URL, environment mappings and truststore material only in private configuration/runtime.
- Do not put actual private addresses, environment names or secrets in this public repository.
- Secret-management cleanup is explicitly deferred; do not change the current private Config Server secret arrangement as part of this implementation increment.

## Starting a new engineering conversation

Read this file, CLAUDE.md, README.md, inbound-api-contract.md, proxy-design.md and the ADRs. Also read the MCP repo's Release 2 document and ADRs. Verify code, CI and deployment state before claiming anything.
