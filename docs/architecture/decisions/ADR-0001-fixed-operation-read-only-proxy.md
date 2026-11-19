# ADR-0001: Fixed-Operation Read-Only Proxy in Front of Portainer

- **Status:** Proposed (remains Proposed until the operator formally approves the architecture)
- **Date:** 2026-11-18
- **Related:** `homelab-management-mcp` ADR-0001 and ADR-0002

## Context

The MCP discovers Docker inventory through the Portainer Server. A Portainer API token inherits its user's permissions and is not inherently read-only, and Portainer CE may not enforce a sufficiently narrow permission set. GET-only application code is not a server-side authorization boundary.

## Decision

Run a small private proxy that holds the Portainer credential and exposes only a fixed catalog of read-only operations.

1. The proxy's inbound API is its own; callers never supply Portainer endpoint IDs, URLs, paths or query strings. The proxy discovers endpoint IDs, resolves them against a server-side environment allowlist, and builds upstream requests from fixed templates.
2. The proxy returns only its own allowlisted DTOs and never forwards raw Portainer response bodies. The MCP's DTO filtering is additional protection, not a substitute.
3. The MCP talks only to the proxy and has no alternate path to Portainer.
4. TLS verification to Portainer stays enabled.
5. The proxy is an independent service: no Proxmox, MCP SDK/tools, Webtools OAuth resource-server stack, central-user authorization, or environment-specific settings in this repository.

## Consequences

- One more service to deploy and monitor.
- The read-only guarantee becomes testable: write methods, unapproved paths and unfiltered fields must be rejected or absent by tests.
- The proxy must be re-validated after Portainer upgrades (upstream paths and shapes may change).
- Do not describe the integration as securely read-only until the boundary has been tested end to end.

## Rejected alternatives

- Rely on a Portainer token plus GET-only MCP code: not server-enforced.
- Mount the Docker socket: a read-only mount does not make the Docker API read-only.
- Pass Portainer responses through unfiltered and rely on the MCP to filter: leaves the boundary incomplete.
