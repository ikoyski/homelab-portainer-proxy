# ADR-0002: Shared Bearer Authentication for the MCP-to-Proxy Hop

- **Status:** Accepted
- **Date:** 2026-11-19
- **Related:** ADR-0001

## Context

The proxy is a private internal service called by the homelab-management-mcp. It needs an explicit caller-authentication mechanism that does not couple it to Webtools OAuth or a user identity system.

## Decision

Use one high-entropy shared bearer secret between the MCP and proxy.

- The MCP sends `Authorization: Bearer <secret>` on every proxy API request.
- The proxy rejects missing, malformed, or incorrect credentials with HTTP 401 before request handling.
- The configured secret must contain at least 32 UTF-8 bytes.
- Compare supplied and expected token bytes using `MessageDigest.isEqual`.
- Store the same value only in private Config Server configuration for both services. Never commit it, bake it into an image, expose it in responses, or log it.
- Keep the proxy on the private Docker network and do not publish its application or management ports to the host or Internet.
- Rotate by updating both services' private configuration and restarting/redeploying them in a coordinated change.

## Consequences

This is simpler to operate than mTLS and matches the existing shared-secret pattern used by internal Spring Boot services. Anyone who obtains the token can impersonate the MCP caller, so the token must be random, high entropy, access-controlled, and transported only over the private network. Network isolation remains required; the token is not a substitute for it.

mTLS may be reconsidered if service identity or key rotation requirements change.
