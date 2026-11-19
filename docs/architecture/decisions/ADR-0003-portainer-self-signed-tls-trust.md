# ADR-0003: Trust the self-signed Portainer TLS certificate

- **Status:** Accepted
- **Date:** 2026-11-19
- **Decision owners:** Homelab operator

## Context

The Portainer Server is reached over HTTPS and presents a self-signed TLS certificate. The proxy must connect to this server without weakening TLS security. The repository is public, so certificate and truststore material must not be committed or baked into the image.

## Decision

Trust the Portainer certificate (or its issuing CA) explicitly through a PKCS#12 truststore created and managed outside the repository and mounted read-only at runtime.

Configure the JVM default truststore using the standard `javax.net.ssl.trustStore`, `javax.net.ssl.trustStoreType` and `javax.net.ssl.trustStorePassword` system properties supplied through the deployment environment. The upstream HTTP client must use the JVM's normal default SSL context.

Certificate-chain validation and hostname/IP verification remain enabled. The certificate SAN must match the exact host used by the configured Portainer base URL. If the base URL uses an IP address, the certificate must contain that IP as an IP SAN.

## Consequences

- No trust-all SSL context, disabled hostname verification, insecure HTTP, or custom permissive verifier is allowed.
- The certificate/CA, generated truststore and truststore password remain outside Git and the container image.
- The truststore should contain only the Portainer trust anchor(s) needed by this proxy.
- Certificate renewal/rotation requires validating and updating the mounted truststore deliberately.
- A certificate SAN mismatch is a deployment/certificate issue to correct, not a reason to weaken TLS verification.

## Operational validation

Before enabling the proxy-to-Portainer client, verify:
1. The certificate was obtained through a trusted administrative channel.
2. The certificate is valid for the exact base URL host (SAN check).
3. The runtime-mounted truststore contains the expected certificate/CA.
4. A successful HTTPS connection works with standard JVM verification.
5. A deliberately untrusted certificate and a hostname/IP mismatch both fail.
