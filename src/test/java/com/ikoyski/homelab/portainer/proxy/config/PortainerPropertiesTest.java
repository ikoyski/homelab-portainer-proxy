package com.ikoyski.homelab.portainer.proxy.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;

class PortainerPropertiesTest {

    @Test
    void acceptsHttpsAndAValidAllowlist() {
        assertDoesNotThrow(() -> new PortainerProperties(
                URI.create("https://portainer.test/"), "test-key",
                3000, 8000, 1024, new PortainerProperties.Environments(List.of("util-pve"))));
    }

    @Test
    void rejectsNonHttpsBaseUrl() {
        assertThrows(IllegalStateException.class, () -> new PortainerProperties(
                URI.create("http://portainer.test/"), "test-key",
                3000, 8000, 1024, new PortainerProperties.Environments(List.of("util-pve"))));
    }

    @Test
    void rejectsMissingApiKey() {
        assertThrows(IllegalStateException.class, () -> new PortainerProperties(
                URI.create("https://portainer.test/"), " ",
                3000, 8000, 1024, new PortainerProperties.Environments(List.of("util-pve"))));
    }

    @Test
    void rejectsDuplicateAllowlistEntries() {
        assertThrows(IllegalStateException.class, () -> new PortainerProperties(
                URI.create("https://portainer.test/"), "test-key",
                3000, 8000, 1024,
                new PortainerProperties.Environments(List.of("util-pve", "util-pve"))));
    }

    @Test
    void rejectsInvalidTimeoutAndResponseLimits() {
        assertThrows(IllegalStateException.class, () -> new PortainerProperties(
                URI.create("https://portainer.test/"), "test-key",
                0, 8000, 1024, new PortainerProperties.Environments(List.of("util-pve"))));
    }
}
