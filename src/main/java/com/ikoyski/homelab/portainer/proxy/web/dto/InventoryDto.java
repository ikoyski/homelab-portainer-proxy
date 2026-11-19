package com.ikoyski.homelab.portainer.proxy.web.dto;

import java.util.List;

/** Aggregate inventory, retaining one host summary for every configured host. */
public record InventoryDto(
        List<HostDto> hosts,
        List<ContainerDto> containers,
        List<NetworkDto> networks) {

    public InventoryDto {
        hosts = List.copyOf(hosts);
        containers = List.copyOf(containers);
        networks = List.copyOf(networks);
    }
}
