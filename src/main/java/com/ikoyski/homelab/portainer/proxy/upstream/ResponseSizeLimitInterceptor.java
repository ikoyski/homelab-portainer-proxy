package com.ikoyski.homelab.portainer.proxy.upstream;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Enforces the configured maximum upstream response size while the body is consumed.
 */
public final class ResponseSizeLimitInterceptor implements ClientHttpRequestInterceptor {

    private final long maxBytes;

    public ResponseSizeLimitInterceptor(long maxBytes) {
        if (maxBytes <= 0) {
            throw new IllegalArgumentException("maxBytes must be positive");
        }
        this.maxBytes = maxBytes;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
            ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response = execution.execute(request, body);
        long contentLength = response.getHeaders().getContentLength();
        if (contentLength > maxBytes) {
            response.close();
            throw new IOException("Upstream response exceeded configured size limit");
        }

        return new LimitedResponse(response, maxBytes);
    }

    private record LimitedResponse(ClientHttpResponse delegate, long maxBytes)
            implements ClientHttpResponse {

        @Override
        public org.springframework.http.HttpStatusCode getStatusCode() throws IOException {
            return delegate.getStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return delegate.getStatusText();
        }

        @Override
        public org.springframework.http.HttpHeaders getHeaders() {
            return delegate.getHeaders();
        }

        @Override
        public InputStream getBody() throws IOException {
            return new BoundedInputStream(delegate.getBody(), maxBytes);
        }

        @Override
        public void close() {
            delegate.close();
        }
    }

    private static final class BoundedInputStream extends FilterInputStream {

        private final long maxBytes;
        private long bytesRead;

        private BoundedInputStream(InputStream input, long maxBytes) {
            super(input);
            this.maxBytes = maxBytes;
        }

        @Override
        public int read() throws IOException {
            int value = super.read();
            if (value != -1) {
                count(1);
            }
            return value;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            if (length == 0) {
                return 0;
            }
            long remainingIncludingProbe = maxBytes - bytesRead + 1;
            int boundedLength = (int) Math.min(length, remainingIncludingProbe);
            int read = super.read(buffer, offset, boundedLength);
            if (read > 0) {
                count(read);
            }
            return read;
        }

        private void count(int count) throws IOException {
            bytesRead += count;
            if (bytesRead > maxBytes) {
                throw new IOException("Upstream response exceeded configured size limit");
            }
        }
    }
}
