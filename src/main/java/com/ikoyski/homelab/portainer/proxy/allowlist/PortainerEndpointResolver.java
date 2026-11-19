package com.ikoyski.homelab.portainer.proxy.allowlist;

import com.ikoyski.homelab.portainer.proxy.config.PortainerProperties;
import com.ikoyski.homelab.portainer.proxy.upstream.PortainerApiClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * Resolves configured proxy host keys to Portainer endpoint IDs discovered at runtime.
 *
 * <p>Portainer endpoint names must exactly match configured host keys. Endpoint IDs
 * remain internal and are never accepted from or returned to an inbound caller.</p>
 */
@Component
public class PortainerEndpointResolver {

    private final PortainerApiClient portainer;
    private final PortainerProperties properties;

    public PortainerEndpointResolver(PortainerApiClient portainer, PortainerProperties properties) {
        this.portainer = portainer;
        this.properties = properties;
    }

    /**
     * Performs a fresh endpoint discovery and returns one resolution for every configured key.
     * Upstream discovery failures are allowed to propagate so the web/service boundary can
     * translate them to a stable sanitized error without leaking upstream details.
     */
    public Map<String, EndpointResolution> resolveAll() {
        JsonNode endpoints = portainer.listEndpoints();
        if (endpoints == null || !endpoints.isArray()) {
            throw new IllegalStateException("Portainer endpoint discovery returned an invalid response");
        }

        Map<String, List<JsonNode>> matches = new LinkedHashMap<>();
        for (String hostKey : properties.environments().allowlist()) {
            matches.put(hostKey, new ArrayList<>());
        }

        for (JsonNode endpoint : endpoints) {
            JsonNode nameNode = endpoint.path("Name");
            if (!nameNode.isString()) {
                continue;
            }
            List<JsonNode> hostMatches = matches.get(nameNode.stringValue());
            if (hostMatches != null) {
                hostMatches.add(endpoint);
            }
        }

        Map<String, EndpointResolution> resolutions = new LinkedHashMap<>();
        matches.forEach((hostKey, hostMatches) -> {
            if (hostMatches.isEmpty()) {
                resolutions.put(hostKey, new EndpointResolution(Status.NOT_FOUND, null));
            } else if (hostMatches.size() > 1) {
                resolutions.put(hostKey, new EndpointResolution(Status.AMBIGUOUS, null));
            } else {
                JsonNode idNode = hostMatches.getFirst().path("Id");
                if (!idNode.isIntegralNumber() || !idNode.canConvertToInt() || idNode.intValue() <= 0) {
                    resolutions.put(hostKey, new EndpointResolution(Status.INVALID_ENDPOINT_ID, null));
                } else {
                    resolutions.put(hostKey, new EndpointResolution(Status.RESOLVED, idNode.intValue()));
                }
            }
        });

        return Collections.unmodifiableMap(resolutions);
    }

    public enum Status {
        RESOLVED,
        NOT_FOUND,
        AMBIGUOUS,
        INVALID_ENDPOINT_ID
    }

    /**
     * Internal adapter result. Do not serialize this type or expose endpointId in web DTOs.
     */
    public record EndpointResolution(Status status, Integer endpointId) {
        public boolean isResolved() {
            return status == Status.RESOLVED && endpointId != null;
        }
    }
}
