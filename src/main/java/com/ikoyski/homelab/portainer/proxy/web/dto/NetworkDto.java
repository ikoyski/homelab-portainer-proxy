package com.ikoyski.homelab.portainer.proxy.web.dto;

import java.util.List;

/** Explicitly allowlisted network inventory fields. */
public record NetworkDto(
        String hostKey,
        String name,
        String driver,
        List<String> connectedContainers,
        List<PublishedPortDto> publishedPorts) {

    public NetworkDto {
        connectedContainers = List.copyOf(connectedContainers);
        publishedPorts = List.copyOf(publishedPorts);
    }
}
