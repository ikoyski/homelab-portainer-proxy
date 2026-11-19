package com.ikoyski.homelab.portainer.proxy.web.dto;

/** Allowlisted host summary; never contains Portainer endpoint identifiers. */
public record HostDto(
        String hostKey,
        HostConnectivity connectivity,
        String dockerEngineVersion,
        String portainerVersion,
        String errorCode) {
}
