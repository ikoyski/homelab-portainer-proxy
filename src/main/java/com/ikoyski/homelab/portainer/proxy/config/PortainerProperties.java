package com.ikoyski.homelab.portainer.proxy.config;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Private runtime configuration for the Portainer upstream.
 * Values are supplied by Config Server; no environment-specific defaults live in code.
 */
@ConfigurationProperties(prefix = "proxy.portainer")
public record PortainerProperties(
        URI baseUrl,
        String apiKey,
        int connectTimeoutMs,
        int readTimeoutMs,
        long maxResponseBytes,
        Environments environments) {

    public PortainerProperties {
        if (baseUrl == null || !"https".equalsIgnoreCase(baseUrl.getScheme())
                || baseUrl.getHost() == null || baseUrl.getUserInfo() != null
                || baseUrl.getQuery() != null || baseUrl.getFragment() != null) {
            throw new IllegalStateException("proxy.portainer.base-url must be a valid HTTPS URL");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("proxy.portainer.api-key must be configured");
        }
        if (connectTimeoutMs <= 0 || readTimeoutMs <= 0 || maxResponseBytes <= 0) {
            throw new IllegalStateException("Portainer timeouts and response-size limit must be positive");
        }
        if (environments == null || environments.allowlist() == null
                || environments.allowlist().isEmpty()) {
            throw new IllegalStateException("proxy.portainer.environments.allowlist must not be empty");
        }
        if (environments.allowlist().stream().anyMatch(key -> key == null
                || !key.matches("[a-z][a-z0-9-]{0,62}"))
                || new HashSet<>(environments.allowlist()).size() != environments.allowlist().size()) {
            throw new IllegalStateException("Portainer environment allowlist contains an invalid or duplicate key");
        }
    }

    public record Environments(List<String> allowlist) {
    }
}
