# Changelog

Notable project changes in reverse chronological order.

## 2026-11-19

### Added
- Accepted ADR-0002: shared bearer authentication for the MCP-to-proxy hop.
- Added a fail-closed inbound bearer-token filter. The configured secret must be at least 32 UTF-8 bytes and is compared using a constant-time comparison.
- Added focused unit tests for accepted, missing, malformed, incorrect and weakly configured tokens.
- Kept the token value exclusively in private Config Server configuration; no secret is committed.

### Verification
- GitHub source changes are committed on the feature branch.
- Maven tests and CI have not yet been run/verified for this change.
- No deployment or live connectivity has been performed.

## 2026-11-18

### Changed
- Skeleton refinements: proxy-owned DTO-only response filtering made a firm requirement; Portainer endpoint IDs declared internal to the proxy; configuration namespace set to `proxy.portainer.*`; application/management port separation documented; repository-independence rules added; license left open.

### Added
- Initial codebase skeleton mirroring `homelab-management-mcp` conventions (Spring Boot 4.1.2, Java 21, Config Client, Actuator, Dockerfile, CI workflow).
- Engineering rules (`CLAUDE.md`), project status, proposed design and ADR-0001 (Proposed).

### Status
- Skeleton only: no proxy functionality; initial local Maven build subsequently reported successful; GitHub Actions CI verification pending; nothing deployed.

## 2026-11-18 (verification update)

### Verified
- The operator reports that the initial local Maven build completed successfully.

### Pending
- GitHub Actions CI has not yet been independently verified.
- Proxy functionality remains unimplemented; nothing has been deployed and this proxy has not contacted Portainer.
