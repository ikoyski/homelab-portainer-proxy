package com.ikoyski.homelab.portainer.proxy.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Enforces the inbound API's default-deny method, path and query-string policy.
 *
 * <p>This is an operation-surface guard, not caller authentication or host allowlist
 * resolution. Those controls must be implemented separately before the proxy is usable.</p>
 */
@Component
public final class ReadOnlyApiFilter extends OncePerRequestFilter {

    private static final String API_PREFIX = "/v1";
    private static final Map<String, String> ERRORS = Map.of(
            "method_not_allowed", "{\"error\":\"method_not_allowed\"}",
            "not_found", "{\"error\":\"not_found\"}",
            "query_parameters_not_allowed", "{\"error\":\"query_parameters_not_allowed\"}"
    );

    private final InboundOperationCatalog catalog;

    public ReadOnlyApiFilter(InboundOperationCatalog catalog) {
        this.catalog = catalog;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();

        if (!isVersionedApiPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (catalog.find(path).isEmpty()) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "not_found");
            return;
        }

        if (!"GET".equals(request.getMethod())) {
            response.setHeader("Allow", "GET");
            writeError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "method_not_allowed");
            return;
        }

        if (request.getQueryString() != null || !request.getParameterMap().isEmpty()) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "query_parameters_not_allowed");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isVersionedApiPath(String path) {
        return API_PREFIX.equals(path) || (path != null && path.startsWith(API_PREFIX + "/"));
    }

    private void writeError(HttpServletResponse response, int status, String errorCode)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(ERRORS.get(errorCode));
    }
}
