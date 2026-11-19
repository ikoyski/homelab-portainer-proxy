# Changelog

Notable project changes in reverse chronological order.

## 2026-11-19

### Added
- Added validated proxy.portainer.* configuration binding for HTTPS base URL, API key, timeouts, response-size limit and environment host-key syntax.
- Added a fixed-operation Portainer API client for endpoint discovery, Docker info, container listing and network listing. All calls use GET; the API key is sent as X-API-Key.
- Added bounded connect/read timeouts and response-size enforcement for both declared and streamed upstream bodies.
- Added tests for configuration validation, fixed upstream paths and API-key header behavior, invalid endpoint IDs, and oversized responses.

### Changed
- Set the proxy application listener to port 8081 and aligned the Dockerfile, README and engineering instructions. Actuator shares the same listener; no separate management port is configured.
- Documented the self-signed Portainer certificate trust model: runtime-mounted PKCS#12 truststore with normal certificate-chain and hostname/IP verification preserved.

### Architecture
- Accepted ADR-0003 for trusting the self-signed Portainer certificate without disabling TLS verification.
- Added deployment guidance for certificate SAN validation and runtime truststore mounting.

### Verification
- The feature-branch tests have not yet been run in this session; do not describe them as passing until CI or a local Maven run confirms.
- Shared bearer authentication follow-up PR #3 merged. Its Maven/CI result still requires independent verification; the earlier SonarCloud authorization failure must not be described as a Maven test failure or a passing test run.
- No deployment or live Portainer connectivity has been performed.

## 2026-11-19 (authentication)

### Added
- Accepted ADR-0002: shared bearer authentication for the MCP-to-proxy hop.
- Added a fail-closed inbound bearer-token filter. The configured secret must be at least 32 UTF-8 bytes and is compared using a constant-time comparison.
- Added focused unit tests for accepted, missing, malformed, incorrect and weakly configured tokens.
- Kept the token value exclusively in private Config Server configuration; no secret is committed.

### Verification
- Shared bearer authentication was merged to master in PR #2.
- The initial PR workflow failed because the Spring application-context smoke test did not supply a test-only bearer token; PR #3 adds that fixture.
- No deployment or live connectivity has been performed.

## 2026-11-18

### Changed
- Skeleton refinements: proxy-owned DTO-only response filtering made a firm requirement; Portainer endpoint IDs declared internal to the proxy; configuration namespace set to proxy.portainer.*; repository-independence rules added; license left open.

### Added
- Initial codebase skeleton mirroring homelab-management-mcp conventions (Spring Boot 4.1.2, Java 21, Config Client, Actuator, Dockerfile, CI workflow).
- Engineering rules (CLAUDE.md), project status, proposed design and ADR-0001 (Proposed).

### Status
- Initial local Maven build reported successful; GitHub Actions CI verification pending; nothing deployed.

## 2026-11-18 (verification update)

### Verified
- The operator reports that the initial local Maven build completed successfully.

### Pending
- GitHub Actions CI has not yet been independently verified.
- Proxy functionality has not been connected to Portainer; nothing has been deployed.

## 2026-11-18 (API contract and request guard)

### Added
- Fixed inbound API contract for GET /v1/hosts, GET /v1/hosts/{hostKey}/containers, GET /v1/hosts/{hostKey}/networks, and GET /v1/inventory.
- Spring request filter and operation catalog that reject unknown /v1 paths, non-GET methods, all query strings and request bodies before handler dispatch.
- Rejection tests for write methods, unknown and Portainer-shaped paths, numeric ID-shaped host keys, path traversal/encoded path attempts, query strings and request bodies.
- Explicit Java response records for host, container, network, published-port and aggregate inventory data, with serialization tests asserting the exact allowlisted field sets.

### Status
- The inbound contract and default-deny request guard are present on master. The initial CI fixes for this slice were incorporated before PR #2. No controllers, Portainer client or deployment have been added.
