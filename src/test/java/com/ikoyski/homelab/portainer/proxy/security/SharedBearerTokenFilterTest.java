package com.ikoyski.homelab.portainer.proxy.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SharedBearerTokenFilterTest {

    private static final String TOKEN = "0123456789abcdef0123456789abcdef";

    @Test
    void acceptsExactBearerToken() {
        var filter = new SharedBearerTokenFilter(TOKEN);
        assertTrue(filter.isAuthorized("Bearer " + TOKEN));
    }

    @Test
    void rejectsMissingMalformedAndIncorrectTokens() {
        var filter = new SharedBearerTokenFilter(TOKEN);
        assertFalse(filter.isAuthorized(null));
        assertFalse(filter.isAuthorized(TOKEN));
        assertFalse(filter.isAuthorized("Basic " + TOKEN));
        assertFalse(filter.isAuthorized("Bearer wrong-token"));
        assertFalse(filter.isAuthorized("Bearer " + TOKEN + " extra"));
    }

    @Test
    void refusesMissingOrWeakConfiguration() {
        assertThrows(IllegalStateException.class, () -> new SharedBearerTokenFilter(""));
        assertThrows(IllegalStateException.class, () -> new SharedBearerTokenFilter("short"));
    }
}
