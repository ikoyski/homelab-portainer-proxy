package com.ikoyski.homelab.portainer.proxy.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ReadOnlyApiFilterTests {

    private final ReadOnlyApiFilter filter =
            new ReadOnlyApiFilter(new InboundOperationCatalog());

    @ParameterizedTest
    @MethodSource("approvedGetRoutes")
    void allowsOnlyCataloguedGetRoutesWithoutQueryStrings(String path) throws Exception {
        FilterResult result = invoke("GET", path, null);

        assertThat(result.response().getStatus()).isEqualTo(200);
        assertThat(result.chain().getRequest()).isNotNull();
    }

    static Stream<Arguments> approvedGetRoutes() {
        return Stream.of(
                Arguments.of("/v1/hosts"),
                Arguments.of("/v1/inventory"),
                Arguments.of("/v1/hosts/util-pve/containers"),
                Arguments.of("/v1/hosts/media-pve/networks")
        );
    }

    @ParameterizedTest
    @MethodSource("writeMethods")
    void rejectsEveryNonGetMethodOnKnownRoutes(String method) throws Exception {
        FilterResult result = invoke(method, "/v1/hosts", null);

        assertThat(result.response().getStatus()).isEqualTo(405);
        assertThat(result.response().getHeader("Allow")).isEqualTo("GET");
        assertThat(result.response().getContentAsString())
                .isEqualTo("{\"error\":\"method_not_allowed\"}");
        assertThat(result.chain().getRequest()).isNull();
    }

    static Stream<Arguments> writeMethods() {
        return Stream.of("POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD");
    }

    @ParameterizedTest
    @MethodSource("unknownPaths")
    void rejectsUnknownPathsAndCallerSuppliedPortainerPaths(String path) throws Exception {
        FilterResult result = invoke("GET", path, null);

        assertThat(result.response().getStatus()).isEqualTo(404);
        assertThat(result.response().getContentAsString())
                .isEqualTo("{\"error\":\"not_found\"}");
        assertThat(result.chain().getRequest()).isNull();
    }

    static Stream<Arguments> unknownPaths() {
        return Stream.of(
                Arguments.of("/v1/unknown"),
                Arguments.of("/v1/hosts/util-pve/containers/123/json"),
                Arguments.of("/v1/endpoints/123/docker/containers"),
                Arguments.of("/v1/hosts/123/containers"),
                Arguments.of("/v1/hosts/util-pve/containers/"),
                Arguments.of("/v1/hosts/util_pve/containers"),
                Arguments.of("/v1/hosts/Util-Pve/networks")
        );
    }

    @ParameterizedTest
    @MethodSource("unapprovedQueries")
    void rejectsAllQueryStringsIncludingDuplicates(String query) throws Exception {
        FilterResult result = invoke("GET", "/v1/hosts", query);

        assertThat(result.response().getStatus()).isEqualTo(400);
        assertThat(result.response().getContentAsString())
                .isEqualTo("{\"error\":\"query_parameters_not_allowed\"}");
        assertThat(result.chain().getRequest()).isNull();
    }

    static Stream<Arguments> unapprovedQueries() {
        return Stream.of(
                Arguments.of("all=true"),
                Arguments.of("endpointId=123"),
                Arguments.of("host=util-pve"),
                Arguments.of("all=true&all=false"),
                Arguments.of("")
        );
    }

    @Test
    void rejectsParsedParametersEvenWhenQueryStringIsUnavailable() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/v1/hosts");
        request.setServletPath("/v1/hosts");
        request.setParameter("unexpected", "value");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(chain.getRequest()).isNull();
    }


    @Test
    void rejectsRequestBodiesOnGetRoutes() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/v1/hosts");
        request.setServletPath("/v1/hosts");
        request.setContent("unexpected".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString())
                .isEqualTo("{\"error\":\"request_body_not_allowed\"}");
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void rejectsChunkedRequestBodiesOnGetRoutes() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/v1/hosts");
        request.setServletPath("/v1/hosts");
        request.addHeader("Transfer-Encoding", "chunked");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString())
                .isEqualTo("{\"error\":\"request_body_not_allowed\"}");
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void doesNotInterceptPathsOutsideVersionedApi() throws Exception {
        FilterResult result = invoke("POST", "/actuator/health", null);

        assertThat(result.response().getStatus()).isEqualTo(200);
        assertThat(result.chain().getRequest()).isNotNull();
    }

    private FilterResult invoke(String method, String path, String query) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(method);
        request.setRequestURI(path);
        request.setServletPath(path);
        if (query != null) {
            request.setQueryString(query);
        }

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        return new FilterResult(response, chain);
    }

    private record FilterResult(MockHttpServletResponse response, MockFilterChain chain) {
    }
}
