package com.ikoyski.homelab.portainer.proxy.web.dto;

/** A single explicitly mapped published-port tuple. */
public record PublishedPortDto(
        Integer hostPort,
        int containerPort,
        String protocol) {

    public PublishedPortDto {
        if (hostPort != null && (hostPort < 1 || hostPort > 65535)) {
            throw new IllegalArgumentException("hostPort must be between 1 and 65535");
        }
        if (containerPort < 1 || containerPort > 65535) {
            throw new IllegalArgumentException("containerPort must be between 1 and 65535");
        }
    }
}
