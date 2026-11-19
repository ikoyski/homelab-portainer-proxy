package com.ikoyski.homelab.portainer.proxy.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Authenticates the private MCP-to-proxy hop using a shared high-entropy bearer secret.
 * The configured value must be supplied by private runtime configuration.
 */
@Component
public class SharedBearerTokenFilter extends OncePerRequestFilter {

    private final byte[] expectedToken;

    public SharedBearerTokenFilter(
            @Value("${proxy.security.shared-token:}") String configuredToken) {
        if (configuredToken == null || configuredToken.isBlank()
                || configuredToken.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "proxy.security.shared-token must be configured with at least 32 UTF-8 bytes");
        }
        this.expectedToken = configuredToken.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Health and metrics are not public API; they must be restricted by the
        // separate, unpublished management listener/network boundary.
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!isAuthorized(authorization)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"code\":\"UNAUTHORIZED\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    boolean isAuthorized(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return false;
        }
        String supplied = authorization.substring(7);
        if (supplied.isBlank() || supplied.indexOf(' ') >= 0) {
            return false;
        }
        return MessageDigest.isEqual(expectedToken, supplied.getBytes(StandardCharsets.UTF_8));
    }
}
