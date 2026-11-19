# Homelab Portainer Proxy

A private, fixed-operation, **read-only** proxy between [homelab-management-mcp](https://github.com/ikoyski/homelab-management-mcp) and the Portainer Server. It returns only proxy-owned allowlisted DTOs and keeps Portainer credentials and endpoint IDs private. See ADR-0001 (proposed).

This repository is public; runtime endpoints, credentials, certificates and truststores must remain outside Git.

## Status

**Release 2 implementation in progress.** Shared bearer authentication, the default-deny inbound request guard, Portainer HTTPS client foundation, fixed upstream GET operations, timeouts and response-size limiting are merged. Dynamic endpoint discovery and exact-name resolution against the private configured host-key allowlist are implemented on the current feature branch. Inbound handlers, DTO mapping and MCP integration remain outstanding. See docs/PROJECT-STATUS.md.

## Technology

Java 21, Spring Boot 4, Spring Cloud Config Client, Spring Boot Actuator with the Prometheus registry. Application name homelab-portainer-proxy. The application and Actuator use the same HTTP listener on port **8081**; expose only that port on the private Docker network.

## Portainer TLS

The Portainer Server uses a self-signed TLS certificate. The proxy must validate it using a runtime-mounted truststore; **do not disable certificate or hostname verification** and do not use trust-all TLS.

1. Obtain the Portainer server certificate or issuing CA certificate through a trusted administrative channel.
2. Check that its Subject Alternative Name (SAN) contains the exact IP address or DNS name used in the configured Portainer base URL. A trusted certificate with a mismatched SAN must still fail validation.
3. Create a PKCS#12 truststore outside this repository and mount it read-only into the proxy container.
4. Configure the JVM's standard truststore properties through the deployment environment, for example via JAVA_TOOL_OPTIONS:
   - -Djavax.net.ssl.trustStore=<mounted-truststore-path>
   - -Djavax.net.ssl.trustStoreType=PKCS12
   - -Djavax.net.ssl.trustStorePassword=<runtime-truststore-password>

Keep the certificate, truststore and password out of this public repository and Docker image. The runtime truststore should contain only the required Portainer trust anchor(s). See ADR-0003.

## Local development

Requires Java 21 and Maven 3.9+.

```bash
mvn -B clean verify
```

Tests do not need the Config Server (src/test/resources/application.yaml disables it). Use the local profile to run against a Config Server.

## Documentation

- CLAUDE.md — mandatory engineering and security rules
- docs/PROJECT-STATUS.md — canonical handoff
- docs/architecture/inbound-api-contract.md — fixed inbound API and DTO contract
- docs/architecture/proxy-design.md — proposed design
- docs/architecture/decisions/ — architecture decision records

## License

GPL-3.0. See LICENSE.
