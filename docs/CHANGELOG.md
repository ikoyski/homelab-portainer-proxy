# Changelog

Notable project changes in reverse chronological order.

## 2026-11-18

### Changed
- Skeleton refinements: proxy-owned DTO-only response filtering made a firm requirement; Portainer endpoint IDs declared internal to the proxy; configuration namespace set to `proxy.portainer.*`; application/management port separation documented; repository-independence rules added; license left open.

### Added
- Initial codebase skeleton mirroring `homelab-management-mcp` conventions (Spring Boot 4.1.2, Java 21, Config Client, Actuator, Dockerfile, CI workflow).
- Engineering rules (`CLAUDE.md`), project status, proposed design and ADR-0001 (Proposed).

### Status
- Skeleton only: no proxy functionality, no first Maven/CI run yet, nothing deployed.

## 2026-11-18 (verification update)

### Verified
- The operator reports that the initial local Maven build completed successfully.

### Pending
- GitHub Actions CI has not yet been independently verified.
- Proxy functionality remains unimplemented; nothing has been deployed and this proxy has not contacted Portainer.
