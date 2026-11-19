package com.ikoyski.homelab.portainer.proxy.web.dto;

/** Explicitly allowlisted container inventory fields. */
public record ContainerDto(
        String hostKey,
        String id,
        String name,
        String image,
        String state,
        String health,
        int restartCount,
        String startedAt,
        Long uptimeSeconds) {

    public ContainerDto {
        if (restartCount < 0) {
            throw new IllegalArgumentException("restartCount must not be negative");
        }
        if (uptimeSeconds != null && uptimeSeconds < 0) {
            throw new IllegalArgumentException("uptimeSeconds must not be negative");
        }
    }
}
