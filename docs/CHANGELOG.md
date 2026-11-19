# Changelog

Notable project changes in reverse chronological order.

## 2026-11-18

### Changed
- Skeleton refinements: proxy-owned DTO-only response filtering made a firm requirement; Portainer endpoint IDs declared internal to the proxy; configuration namespace set to `proxy.portainer.*`; application/management port separation documented; repository-independence rules added; license left open.

### Added
- Initial codebase skeleton mirroring `homelab-management-mcp` conventions (Spring Boot 4.1.2, Java 21, Config Client, Actuator, Dockerfile, CI workflow).
- Engineering rules (`CLAUDE.md`), project status, proposed design and ADR-0001 (Proposed).

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
- Fixed inbound API contract for `GET /v1/hosts`, `GET /v1/hosts/{hostKey}/containers`, `GET /v1/hosts/{hostKey}/networks`, and `GET /v1/inventory`.
- Spring request filter and operation catalog that reject unknown `/v1` paths, non-GET methods and all query strings before handler dispatch.
- Rejection tests for write methods, unknown and Portainer-shaped paths, numeric ID-shaped host keys, and query strings.

### Status
- Changes are on `feature/api-contract-default-deny` for review. Tests have not yet been run in this environment; GitHub Actions CI is pending. No controllers, Portainer client, caller authentication or deployment have been added.
