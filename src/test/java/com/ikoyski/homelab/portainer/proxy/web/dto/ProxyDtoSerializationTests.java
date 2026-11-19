package com.ikoyski.homelab.portainer.proxy.web.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ProxyDtoSerializationTests {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesOnlyExplicitlyDeclaredInventoryFields() throws Exception {
        InventoryDto inventory = new InventoryDto(
                List.of(new HostDto("util-pve", HostConnectivity.UP, "29.1.3", "2.45.4", null)),
                List.of(new ContainerDto("util-pve", "container-id", "sample", "nginx:latest",
                        "running", "healthy", 0, "2026-11-18T10:00:00Z", 3600L)),
                List.of(new NetworkDto("util-pve", "bridge", "bridge",
                        List.of("sample"), List.of(new PublishedPortDto(8080, 80, "tcp")))));

        JsonNode root = objectMapper.readTree(objectMapper.writeValueAsBytes(inventory));

        assertThat(fieldNames(root)).containsExactlyInAnyOrder("hosts", "containers", "networks");
        assertThat(fieldNames(root.get("hosts").get(0)))
                .containsExactlyInAnyOrder("hostKey", "connectivity", "dockerEngineVersion",
                        "portainerVersion", "errorCode");
        assertThat(fieldNames(root.get("containers").get(0)))
                .containsExactlyInAnyOrder("hostKey", "id", "name", "image", "state", "health",
                        "restartCount", "startedAt", "uptimeSeconds");
        assertThat(fieldNames(root.get("networks").get(0)))
                .containsExactlyInAnyOrder("hostKey", "name", "driver", "connectedContainers",
                        "publishedPorts");
        assertThat(fieldNames(root.get("networks").get(0).get("publishedPorts").get(0)))
                .containsExactlyInAnyOrder("hostPort", "containerPort", "protocol");

        String json = root.toString();
        assertThat(json).doesNotContain("endpointId", "apiKey", "environment", "labels",
                "mounts", "command", "entrypoint", "logs", "inspect");
    }

    @Test
    void rejectsNegativeRestartCountsAndUptime() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                new ContainerDto("host-a", "id", "name", "image", "running", null,
                        -1, null, 0L)).isInstanceOf(IllegalArgumentException.class);

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                new ContainerDto("host-a", "id", "name", "image", "running", null,
                        0, null, -1L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsInvalidPublishedPortNumbers() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                new PublishedPortDto(0, 80, "tcp"))
                .isInstanceOf(IllegalArgumentException.class);

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                new PublishedPortDto(null, 65536, "tcp"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Set<String> fieldNames(JsonNode node) {
        Set<String> names = new HashSet<>();
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }
}
