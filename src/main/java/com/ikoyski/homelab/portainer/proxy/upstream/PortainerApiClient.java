package com.ikoyski.homelab.portainer.proxy.upstream;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Fixed-operation Portainer API client. Callers cannot supply paths, URLs or headers.
 * Portainer endpoint IDs are accepted only as internal adapter values discovered by
 * the proxy; they are never part of the inbound API.
 */
@Component
public class PortainerApiClient {

    private final RestClient restClient;

    public PortainerApiClient(RestClient portainerRestClient) {
        this.restClient = portainerRestClient;
    }

    public JsonNode listEndpoints() {
        return get("/api/endpoints");
    }

    public JsonNode getDockerInfo(int discoveredEndpointId) {
        return getEndpointResource(discoveredEndpointId, "info");
    }

    public JsonNode listContainers(int discoveredEndpointId) {
        return restClient.get()
                .uri("/api/endpoints/{id}/docker/containers/json?all=true", requireEndpointId(discoveredEndpointId))
                .retrieve()
                .body(JsonNode.class);
    }

    public JsonNode listNetworks(int discoveredEndpointId) {
        return getEndpointResource(discoveredEndpointId, "networks");
    }

    private JsonNode getEndpointResource(int endpointId, String resource) {
        int validId = requireEndpointId(endpointId);
        return get("/api/endpoints/" + validId + "/docker/" + resource);
    }

    private JsonNode get(String fixedPath) {
        return restClient.get().uri(fixedPath).retrieve().body(JsonNode.class);
    }

    private int requireEndpointId(int endpointId) {
        if (endpointId <= 0) {
            throw new IllegalArgumentException("Discovered Portainer endpoint ID must be positive");
        }
        return endpointId;
    }
}
