# Homelab Portainer Proxy

A private, fixed-operation, **read-only** proxy between [homelab-management-mcp](https://github.com/ikoyski/homelab-management-mcp) and the Portainer Server. It is intended to return only proxy-owned allowlisted DTOs and keep Portainer credentials and endpoint IDs private. See [ADR-0001](docs/architecture/decisions/ADR-0001-fixed-operation-read-only-proxy.md) (proposed).

This repository is public; runtime endpoints, credentials, certificates and truststores must remain outside Git.

## Status

**Release 2 foundation in progress.** Shared bearer authentication is merged to `master`. Its first GitHub Actions run failed because the Spring context smoke test lacked a test-only token; a follow-up fixes the test configuration and CI verification is pending. The fixed inbound API contract and initial default-deny request guard are present. Portainer operations, upstream integration and MCP integration remain unimplemented. See [docs/PROJECT-STATUS.md](docs/PROJECT-STATUS.md).

## Technology

Java 21, Spring Boot 4, Spring Cloud Config Client, Spring Boot Actuator with the Prometheus registry. Application name `homelab-portainer-proxy`, application port 8084; Actuator runs on a separate internal management port that is never published.

## Local development

Requires Java 21 and Maven 3.9+.

```bash
mvn -B clean verify
```

Tests do not need the Config Server (`src/test/resources/application.yaml` disables it). Use the `local` profile to run against a Config Server.

## Documentation

- [CLAUDE.md](CLAUDE.md) — mandatory engineering and security rules
- [docs/PROJECT-STATUS.md](docs/PROJECT-STATUS.md) — canonical handoff
- [docs/architecture/inbound-api-contract.md](docs/architecture/inbound-api-contract.md) — fixed inbound API and DTO contract
- [docs/architecture/proxy-design.md](docs/architecture/proxy-design.md) — proposed design
- [docs/CHANGELOG.md](docs/CHANGELOG.md)

## License

GPL-3.0. See [LICENSE](LICENSE).
