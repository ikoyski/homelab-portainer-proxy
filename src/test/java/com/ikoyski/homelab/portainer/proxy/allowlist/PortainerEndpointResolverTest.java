package com.ikoyski.homelab.portainer.proxy.allowlist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ikoyski.homelab.portainer.proxy.config.PortainerProperties;
import com.ikoyski.homelab.portainer.proxy.upstream.PortainerApiClient;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class PortainerEndpointResolverTest {

    private final JsonMapper mapper = new JsonMapper();

    @Test
    void resolvesOnlyExactConfiguredHostNamesAndKeepsEndpointIdsInternalToResolution() throws Exception {
        PortainerApiClient client = mock(PortainerApiClient.class);
        when(client.listEndpoints()).thenReturn(mapper.readTree("""
                [
                  {"Id": 11, "Name": "util-pve", "URL": "tcp://ignored"},
                  {"Id": 12, "Name": "dev-pve"},
                  {"Id": 99, "Name": "shared-pve"},
                  {"Id": 13, "Name": "UTIL-PVE"}
                ]
                """));
        PortainerEndpointResolver resolver = resolver(client, "util-pve", "dev-pve", "media-pve");

        Map<String, PortainerEndpointResolver.EndpointResolution> result = resolver.resolveAll();

        assertEquals(PortainerEndpointResolver.Status.RESOLVED, result.get("util-pve").status());
        assertEquals(11, result.get("util-pve").endpointId());
        assertEquals(PortainerEndpointResolver.Status.RESOLVED, result.get("dev-pve").status());
        assertEquals(12, result.get("dev-pve").endpointId());
        assertEquals(PortainerEndpointResolver.Status.NOT_FOUND, result.get("media-pve").status());
        assertFalse(result.containsKey("shared-pve"));
        assertThrows(UnsupportedOperationException.class,
                () -> result.put("shared-pve", new PortainerEndpointResolver.EndpointResolution(
                        PortainerEndpointResolver.Status.RESOLVED, 99)));
    }

    @Test
    void marksDuplicateEndpointNamesAmbiguousInsteadOfChoosingOne() throws Exception {
        PortainerApiClient client = mock(PortainerApiClient.class);
        when(client.listEndpoints()).thenReturn(mapper.readTree("""
                [{"Id": 1, "Name": "util-pve"}, {"Id": 2, "Name": "util-pve"}]
                """));

        PortainerEndpointResolver.EndpointResolution result =
                resolver(client, "util-pve").resolveAll().get("util-pve");

        assertEquals(PortainerEndpointResolver.Status.AMBIGUOUS, result.status());
        assertTrue(result.endpointId() == null);
    }

    @Test
    void rejectsAnInvalidIdForAnOtherwiseMatchingEndpoint() throws Exception {
        PortainerApiClient client = mock(PortainerApiClient.class);
        when(client.listEndpoints()).thenReturn(mapper.readTree("""
                [{"Id": "not-a-number", "Name": "util-pve"}]
                """));

        PortainerEndpointResolver.EndpointResolution result =
                resolver(client, "util-pve").resolveAll().get("util-pve");

        assertEquals(PortainerEndpointResolver.Status.INVALID_ENDPOINT_ID, result.status());
        assertTrue(result.endpointId() == null);
    }

    @Test
    void rejectsNonArrayDiscoveryResponse() throws Exception {
        PortainerApiClient client = mock(PortainerApiClient.class);
        when(client.listEndpoints()).thenReturn(mapper.readTree("{\"data\": []}"));

        assertThrows(IllegalStateException.class, () -> resolver(client, "util-pve").resolveAll());
    }

    private PortainerEndpointResolver resolver(PortainerApiClient client, String... hostKeys) {
        PortainerProperties properties = new PortainerProperties(
                URI.create("https://portainer.test/"),
                "test-api-key",
                3000,
                8000,
                2048,
                new PortainerProperties.Environments(List.of(hostKeys)));
        return new PortainerEndpointResolver(client, properties);
    }
}
