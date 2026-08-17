package com.vitaledge.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.vitaledge.common.util.EmailNormalizer;
import org.junit.jupiter.api.Test;

class EmailNormalizerTest {

    @Test
    void normalizesCaseAndWhitespace() {
        assertEquals("user@example.com", EmailNormalizer.normalize("  User@Example.COM  "));
    }

    @Test
    void handlesNull() {
        assertNull(EmailNormalizer.normalize(null));
    }

    @Test
    void appliesNfkc() {
        // Full-width '@' (U+FF20) normalizes to ASCII '@'
        assertEquals("user@example.com", EmailNormalizer.normalize("user\uff20example.com"));
    }
}