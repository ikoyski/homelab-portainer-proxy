package com.ikoyski.homelab.portainer.proxy.upstream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

class ResponseSizeLimitInterceptorTest {

    @Test
    void rejectsOversizedDeclaredContentLength() throws Exception {
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(11);
        when(response.getHeaders()).thenReturn(headers);

        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        when(execution.execute(any(HttpRequest.class), any())).thenReturn(response);

        ResponseSizeLimitInterceptor interceptor = new ResponseSizeLimitInterceptor(10);
        assertThrows(IOException.class,
                () -> interceptor.intercept(mock(HttpRequest.class), new byte[0], execution));
        verify(response).close();
    }

    @Test
    void rejectsOversizedStreamingBodyWithoutContentLength() throws Exception {
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getHeaders()).thenReturn(new HttpHeaders());
        when(response.getBody()).thenReturn(new ByteArrayInputStream(new byte[11]));

        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        when(execution.execute(any(HttpRequest.class), any())).thenReturn(response);

        ResponseSizeLimitInterceptor interceptor = new ResponseSizeLimitInterceptor(10);
        ClientHttpResponse limited = interceptor.intercept(
                mock(HttpRequest.class), new byte[0], execution);

        assertThrows(IOException.class, () -> limited.getBody().readAllBytes());
    }
}
