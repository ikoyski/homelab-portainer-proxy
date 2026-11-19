# Proxy design (proposed)

Status: proposed baseline; nothing here is implemented. Not yet formally approved (see ADR-0001).

## Goals
- Expose only fixed read-only Docker inventory operations.
- Return only proxy-owned, allowlisted DTOs. Never forward raw Portainer bodies.
- Keep the Portainer credential and Portainer endpoint IDs private to the proxy.
- Fail closed: unknown method, path, or query parameter is rejected.
- Keep TLS certificate and hostname verification to Portainer.

## Topology

    homelab-management-mcp --(private network, authenticated)--> homelab-portainer-proxy --(HTTPS, verified)--> Portainer Server

## Firm requirements

### 1. Response filtering
The proxy maps each upstream Portainer/Docker response into its own DTOs, copying explicitly allowlisted fields only. Raw upstream bodies are never forwarded, streamed, or echoed, including in errors. The MCP also filters into its own DTOs; that is additional protection and never a reason to relax filtering in the proxy. Fields such as environment variables, labels, commands, mounts, logs, raw inspect payloads and secrets must be structurally impossible to return, because no DTO has a field for them.

### 2. Environment IDs are internal
Portainer `/api/endpoints/{id}/...` paths are **outbound upstream requests only**. They are not part of the proxy's inbound API and the MCP never supplies endpoint IDs. The proxy:
1. discovers Portainer environments (and their IDs) at runtime,
2. resolves them against its server-side environment allowlist (private configuration),
3. constructs every upstream request itself from fixed templates.

Callers identify an environment, if at all, only by a configured proxy-level key validated against the allowlist. Unknown keys are rejected. Endpoint IDs are not exposed as stable identifiers.

## Inbound API (proposed shape, to be finalized before implementation)
Proxy-owned, versioned, GET-only operations returning proxy DTOs, for example:

| Operation | Purpose |
|---|---|
| list environments | configured environments and availability |
| get environment info | allowlisted Docker engine info for one environment |
| list containers | allowlisted container fields for one environment |
| list networks | allowlisted network fields for one environment |

Exact paths, DTO fields and the aggregate operation are decided in the implementation design. No path variable or parameter may carry an upstream path, URL or endpoint ID.

## Fixed upstream request templates (outbound only; verify against the deployed Portainer API)

| Method | Upstream template | Query |
|---|---|---|
| GET | `/api/endpoints` | none / fixed |
| GET | `/api/endpoints/{discoveredId}/docker/info` | none |
| GET | `/api/endpoints/{discoveredId}/docker/containers/json` | fixed `all=true` |
| GET | `/api/endpoints/{discoveredId}/docker/containers/{containerId}/json` | none |
| GET | `/api/endpoints/{discoveredId}/docker/networks` | none |

`{discoveredId}` comes only from proxy-side discovery plus allowlist match. `{containerId}` is validated by a strict pattern and originates from a previous upstream listing, not from caller input.

## Rejection behavior
- Non-GET methods: 405. Unknown path or any unexpected query parameter: rejected, nothing forwarded.
- Errors use stable sanitized codes; no upstream bodies, headers or exception messages.

## Repository independence
The proxy contains no Proxmox integration, MCP SDK or tool code, Webtools OAuth resource-server configuration, central-user authorization, or environment-specific settings. Only common Spring infrastructure is retained (web, REST client, Config Client, Actuator/Prometheus).

## Ports
- Application port: 8084.
- Actuator management port: separate, internal only, never published publicly. The value is set through private runtime configuration and is not defined in this repository; confirm the intended configuration before choosing one.

## Open decisions
1. **Caller authentication** between the MCP and proxy (e.g. shared bearer secret, mTLS). Must not depend on the Webtools OAuth resource-server stack in this repository unless explicitly decided later. A private network alone is not sufficient.
2. **Portainer credential type** and least-privilege user, confirmed on the installed edition.
3. **Limits:** response-size cap, timeouts, concurrency.
4. **Exact inbound API paths and DTO fields.**
5. **License:** awaiting operator confirmation.

## Configuration shape (illustrative only; real values live in private runtime configuration)

```yaml
proxy:
  portainer:
    base-url: ${PROXY_PORTAINER_BASE_URL}
    api-key: ${PROXY_PORTAINER_API_KEY}
    connect-timeout-ms: 3000
    read-timeout-ms: 8000
    environments:
      - key: <proxy-level-key>
        portainer-name: <name as shown in Portainer>
```

Environment names and mappings belong only in private configuration.

## Test plan
Rejected methods, paths and query parameters; endpoint IDs impossible to supply; DTO-only output with sensitive upstream fields absent; credential non-leakage in responses and logs; per-environment failure isolation; upstream timeout/TLS/5xx mapping; oversized response handling.
