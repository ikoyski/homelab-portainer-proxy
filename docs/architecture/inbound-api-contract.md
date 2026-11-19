# Inbound API Contract (proposed)

Status: proposed contract for the first implementation increment. It does not approve ADR-0001 or settle caller authentication.

## Contract principles

- Base path: `/v1`.
- GET only, with no request body or query string. The proxy never forwards caller-supplied headers upstream. Callers cannot supply an upstream URL, Portainer endpoint ID, Docker API path, or command.
- The only caller-supplied environment identifier is a proxy-level `hostKey`. It must match the syntax below and then resolve against the private server-side allowlist. Syntax validation alone does not authorize a host.
- Portainer endpoint IDs are discovered and resolved inside the proxy. They never appear in inbound routes or response DTOs.
- Responses are proxy-owned DTOs populated field by field. Never return raw Portainer/Docker JSON, maps of arbitrary properties, or upstream error bodies.
- Unknown paths return 404; non-GET methods on a known path return 405 with `Allow: GET`; any query string or request body returns 400. All rejections happen before an upstream request can be made.
- Stable error bodies contain only a code: `{"error":"not_found"}`, `{"error":"method_not_allowed"}`, `{"error":"query_parameters_not_allowed"}`, or `{"error":"request_body_not_allowed"}`.

## Fixed inbound operation catalog

| Method | Path | Purpose |
|---|---|---|
| GET | `/v1/hosts` | List configured proxy host keys and host connectivity/version summary. |
| GET | `/v1/hosts/{hostKey}/containers` | List allowlisted container inventory for one configured host. |
| GET | `/v1/hosts/{hostKey}/networks` | List allowlisted network inventory for one configured host. |
| GET | `/v1/inventory` | Aggregate host, container and network inventory across configured hosts. |

No other inbound operation is part of this contract. In particular, upstream Portainer routes such as `/api/endpoints/{id}/docker/...` are outbound-only templates.

### hostKey syntax

`[a-z][a-z0-9-]{0,62}`

The key is a proxy configuration alias, not a Portainer endpoint ID, Docker ID, hostname, URL, or arbitrary endpoint selector. The implementation must require an exact match in the private configured allowlist before any upstream operation.

## Response DTOs

These are the only fields the proxy may expose. Nullable values are explicitly optional; unknown upstream fields are discarded.

### HostDto

| Field | Type | Meaning |
|---|---|---|
| `hostKey` | string | Configured proxy alias. |
| `connectivity` | enum: `UP`, `UNAVAILABLE`, `UNKNOWN` | Sanitized connectivity state. |
| `dockerEngineVersion` | string, optional | Docker Engine version from the allowlisted info response. |
| `portainerVersion` | string, optional | Portainer Server version. |
| `errorCode` | string, optional | Stable proxy-defined code; never exception text or upstream body. |

### ContainerDto

| Field | Type | Meaning |
|---|---|---|
| `hostKey` | string | Configured proxy alias. |
| `id` | string | Docker container ID. |
| `name` | string | Normalized container name. |
| `image` | string | Image reference. |
| `state` | string | Container state. |
| `health` | string, optional | Health status when available. |
| `restartCount` | integer | Restart count. |
| `startedAt` | string, optional | Start timestamp in ISO-8601 form. |
| `uptimeSeconds` | integer, optional | Derived uptime when start time is valid. |

### NetworkDto

| Field | Type | Meaning |
|---|---|---|
| `hostKey` | string | Configured proxy alias. |
| `name` | string | Network name. |
| `driver` | string | Network driver. |
| `connectedContainers` | array of strings | Normalized connected container names. |
| `publishedPorts` | array of PublishedPortDto | Explicitly mapped published port tuples. |

### PublishedPortDto

| Field | Type | Meaning |
|---|---|---|
| `hostPort` | integer, optional | Published host port. |
| `containerPort` | integer | Container port. |
| `protocol` | string | Protocol, normalized to the supported Docker protocol value. |

### InventoryDto

| Field | Type | Meaning |
|---|---|---|
| `hosts` | array of HostDto | One result per configured host, including unavailable hosts. |
| `containers` | array of ContainerDto | Successfully retrieved containers. |
| `networks` | array of NetworkDto | Successfully retrieved networks. |

A failed host must not suppress successful results from other hosts. Its failure is represented in its HostDto using `connectivity=UNAVAILABLE` and a stable `errorCode`; do not include exception messages or upstream response content.

## Explicitly excluded response data

No DTO may expose environment variables, labels, commands, entrypoints, mounts, volume details, logs, exec results, raw inspect payloads, arbitrary Docker metadata, Portainer endpoint IDs, Portainer credentials, upstream headers, or exception messages. Do not add generic `Map<String, Object>` payload fields.

## Request validation and rejection matrix

| Request | Result |
|---|---|
| Known path + GET + no query string | Continue to handler. |
| Known path + POST/PUT/PATCH/DELETE/OPTIONS/HEAD | 405, `Allow: GET`. |
| Unknown path under `/v1` | 404. |
| Unknown or extra path segment | 404. |
| Numeric ID in place of hostKey | 404. |
| Any query string, including duplicate parameters | 400. |
| Any request body, including chunked transfer | 400. |
| Portainer endpoint path supplied by caller | 404. |

Authentication is intentionally not specified here. The API must not be exposed until caller authentication and the private network boundary are designed and implemented.
