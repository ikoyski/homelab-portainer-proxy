/**
 * Outbound Portainer client. Discovers environment IDs, builds every upstream request itself
 * from fixed templates, holds the Portainer credential privately, and maps upstream payloads
 * into proxy-owned DTOs. Never returns the credential, upstream headers, raw bodies or raw
 * error bodies. Placeholder until implementation starts.
 */
package com.ikoyski.homelab.portainer.proxy.upstream;
