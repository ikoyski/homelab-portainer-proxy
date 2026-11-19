package com.ikoyski.homelab.portainer.proxy.web;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * The complete inbound operation catalog.
 *
 * <p>Keep this catalog deliberately small. Upstream Portainer paths and endpoint IDs
 * must never be added here; they belong exclusively to the upstream adapter.</p>
 */
@Component
public final class InboundOperationCatalog {

    private static final Pattern HOST_CONTAINERS = Pattern.compile(
            "/v1/hosts/([a-z][a-z0-9-]{0,62})/containers");
    private static final Pattern HOST_NETWORKS = Pattern.compile(
            "/v1/hosts/([a-z][a-z0-9-]{0,62})/networks");

    private static final List<Operation> OPERATIONS = List.of(
            new Operation("GET", "/v1/hosts", "list_hosts"),
            new Operation("GET", "/v1/inventory", "get_inventory")
    );

    public Optional<Operation> find(String path) {
        if (path == null) {
            return Optional.empty();
        }

        for (Operation operation : OPERATIONS) {
            if (operation.pathTemplate().equals(path)) {
                return Optional.of(operation);
            }
        }

        if (HOST_CONTAINERS.matcher(path).matches()) {
            return Optional.of(new Operation("GET", "/v1/hosts/{hostKey}/containers",
                    "list_containers"));
        }

        if (HOST_NETWORKS.matcher(path).matches()) {
            return Optional.of(new Operation("GET", "/v1/hosts/{hostKey}/networks",
                    "list_networks"));
        }

        return Optional.empty();
    }

    public record Operation(String method, String pathTemplate, String operationId) {
    }
}
