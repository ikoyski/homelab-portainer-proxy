# Homelab Portainer Proxy

A private, fixed-operation, **read-only** proxy between [homelab-management-mcp](https://github.com/ikoyski/homelab-management-mcp) and the Portainer Server. It returns only its own allowlisted DTOs, keeps the Portainer credential and Portainer environment IDs private, and rejects everything outside a fixed catalog. Rationale: [ADR-0001](docs/architecture/decisions/ADR-0001-fixed-operation-read-only-proxy.md) (proposed).

This repository is public; runtime endpoints, credentials, certificates and truststores must remain outside Git.

## Status

**Skeleton only.** No proxy functionality is implemented, and it has not yet had its first Maven build or CI run. See [docs/PROJECT-STATUS.md](docs/PROJECT-STATUS.md).

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
- [docs/architecture/proxy-design.md](docs/architecture/proxy-design.md) — proposed design
- [docs/CHANGELOG.md](docs/CHANGELOG.md)

## License

To be decided by the repository owner.
