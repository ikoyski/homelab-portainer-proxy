package com.ikoyski.homelab.portainer.proxy.upstream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class PortainerApiClientTest {

    @Test
    void usesOnlyFixedGetOperationsAndThePortainerApiKeyHeader() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://portainer.test")
                .defaultHeader("X-API-Key", "test-api-key");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        PortainerApiClient client = new PortainerApiClient(builder.build());

        server.expect(requestTo("https://portainer.test/api/endpoints"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-API-Key", "test-api-key"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        server.expect(requestTo("https://portainer.test/api/endpoints/7/docker/info"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-API-Key", "test-api-key"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        server.expect(requestTo("https://portainer.test/api/endpoints/7/docker/containers/json?all=true"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-API-Key", "test-api-key"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        server.expect(requestTo("https://portainer.test/api/endpoints/7/docker/networks"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-API-Key", "test-api-key"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertNotNull(client.listEndpoints());
        assertNotNull(client.getDockerInfo(7));
        assertNotNull(client.listContainers(7));
        assertNotNull(client.listNetworks(7));
        server.verify();
    }

    @Test
    void rejectsInvalidEndpointIdsBeforeMakingRequests() {
        PortainerApiClient client = new PortainerApiClient(
                RestClient.builder().baseUrl("https://portainer.test").build());

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class, () -> client.getDockerInfo(0));
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class, () -> client.listContainers(-1));
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class, () -> client.listNetworks(0));
    }
}
