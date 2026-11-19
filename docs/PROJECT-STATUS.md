# Project Status and Handoff

> **Purpose:** Canonical continuity record for maintainers and AI coding assistants.
>
> **Last updated:** 2026-11-18
> **Repository:** `ikoyski/homelab-portainer-proxy`
> **Current focus:** Skeleton refined; awaiting first Maven build and CI verification

## Current state

| Area | Status | Notes |
|---|---|---|
| Codebase skeleton | Created and refined; **not yet built** | Maven project, Dockerfile, CI workflow, docs, package layout, context-load smoke test. First `mvn verify` and CI run still pending. |
| Proxy functionality | Not started | `allowlist`, `upstream`, `web`, `security`, `config` are empty placeholders. |
| Architecture | Proposed | ADR-0001 stays Proposed until formally approved. |
| Caller authentication | Open decision | See design doc. |
| License | Open | Awaiting operator confirmation; no LICENSE file added. |
| Deployment | None | Nothing deployed; Portainer never contacted. |

## Settled design requirements (proposed architecture)

- The proxy returns only its own allowlisted DTOs; raw Portainer bodies are never forwarded. MCP-side filtering is additional protection only.
- Portainer `/api/endpoints/{id}/...` routes are outbound upstream requests only. The MCP never supplies endpoint IDs; the proxy discovers them, resolves them against its server-side environment allowlist, and builds upstream requests itself.
- Configuration namespace is `proxy.portainer.*`; application name is `homelab-portainer-proxy`.
- Application port 8084; Actuator uses a separate internal management port that is never published. Its value is not defined in this repo.
- The repository is independent of the MCP: no Proxmox, MCP SDK/tools, Webtools OAuth resource server, central-user authorization, or environment-specific settings.

## Open items

1. Caller authentication mechanism and network placement.
2. Portainer credential type and least-privilege user on the installed edition.
3. Exact inbound API paths and DTO fields; response-size, timeout and concurrency limits.
4. Management port value, to be taken from the intended private runtime configuration.
5. Java trust for the self-signed Portainer certificate (private runtime truststore).
6. License choice (operator).
7. Formal approval of ADR-0001.

## Recommended next work

1. Run `mvn -B clean verify` and CI; fix anything the first build reveals.
2. Define inbound API and DTOs, then write the operation catalog and rejection tests first (default-deny).
3. Implement endpoint discovery, allowlist resolution and the upstream client.
4. Implement DTO mapping and sanitized error handling.
5. Security tests, then integration with homelab-management-mcp; deploy only with explicit operator approval.

## Starting a new engineering conversation
Read this file, `CLAUDE.md`, `README.md`, `docs/architecture/proxy-design.md`, then the ADRs. Also read the MCP repo's Release 2 document and its ADR-0001/0002. Verify code, CI and deployment state before claiming anything.
