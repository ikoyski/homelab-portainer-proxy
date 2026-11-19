# Project Status and Handoff

> **Purpose:** Canonical continuity record for maintainers and AI coding assistants.
>
> **Last updated:** 2026-11-18
> **Repository:** `ikoyski/homelab-portainer-proxy`
> **Current focus:** Review API contract and default-deny request-surface guard; run CI

## Current state

| Area | Status | Notes |
|---|---|---|
| Initial skeleton | Built locally | The operator reported a successful initial Maven build. |
| API contract | Defined on review branch | See `docs/architecture/inbound-api-contract.md`; four fixed GET operations and proxy-owned DTO fields. |
| Request-surface guard | Implemented on review branch | `InboundOperationCatalog` and `ReadOnlyApiFilter` reject unknown paths, non-GET methods, all query strings and request bodies. |
| Rejection / DTO tests | Added on review branch; CI runs exposed three test/build issues (JUnit argument source, missing Spring Boot 4 Jackson starter, and a Jackson 2-to-3 tree API change); fixes are pushed, latest run pending | Covers route/method/path/query/body rejection and exact serialized DTO field sets. |
| GitHub Actions CI | Pending | Verify the review branch workflow before merging. |
| Response DTO Java records | Implemented on review branch | Explicit Host, Container, Network, PublishedPort and Inventory records; serialization tests assert only allowlisted fields. |
| Portainer integration | Not started | No Portainer client, credentials, endpoint discovery or upstream requests. |
| Caller authentication | Open decision | Must be decided and implemented before exposing the service. |
| Architecture | Proposed | ADR-0001 remains Proposed until formally approved. |
| License | Open | Awaiting operator confirmation; no LICENSE file added. |
| Deployment | None | Nothing deployed; this proxy has not contacted Portainer. |

## Current implementation boundary

The request filter is an early guard for the `/v1` URL space. It is not caller authentication, configured host allowlist resolution, a complete authorization boundary, or proof of end-to-end read-only behavior. No API controllers or upstream-to-DTO mapping exist yet. Do not expose or deploy this service as usable until authentication, host allowlist resolution, handler implementation, upstream restrictions and integration tests are implemented.

## Settled design requirements (proposed architecture)

- The proxy returns only its own allowlisted DTOs; raw Portainer bodies are never forwarded. MCP-side filtering is additional protection only.
- Portainer `/api/endpoints/{id}/...` routes are outbound upstream requests only. The MCP never supplies endpoint IDs; the proxy discovers them, resolves them against its server-side environment allowlist, and builds upstream requests itself.
- Configuration namespace is `proxy.portainer.*`; application name is `homelab-portainer-proxy`.
- Application port 8084; Actuator uses a separate internal management port that is never published. Its value is not defined in this repo.
- The repository is independent of the MCP: no Proxmox, MCP SDK/tools, Webtools OAuth resource server, central-user authorization, or environment-specific settings.

## Open items

1. Caller authentication mechanism and network placement.
2. Exact private host-key allowlist and mapping to discovered Portainer endpoint IDs.
3. Portainer credential type and least-privilege user on the installed edition.
4. Controller behavior, upstream-to-DTO mapping, response-size cap, timeouts and concurrency limits.
5. Management port value, to be taken from the intended private runtime configuration.
6. Java trust for the self-signed Portainer certificate (private runtime truststore).
7. License choice (operator).
8. Formal approval of ADR-0001.

## Recommended next work

1. Run Maven tests and verify GitHub Actions on the review branch; fix any failures before merge.
2. Decide caller authentication and implement exact configured-host allowlist resolution.
3. Implement controllers and DTO mapping from typed upstream models, preserving per-host failure isolation.
4. Implement Portainer endpoint discovery and the fixed upstream client with verified TLS, bounded timeouts and response-size limits.
5. Add security, sanitization, outage/recovery and integration tests; deploy only with explicit operator approval.

## Starting a new engineering conversation

Read this file, `CLAUDE.md`, `README.md`, `docs/architecture/inbound-api-contract.md`, `docs/architecture/proxy-design.md`, then the ADRs. Also read the MCP repo's Release 2 document and its ADR-0001/0002. Verify code, CI and deployment state before claiming anything.
