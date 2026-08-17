package com.vitaledge.common.util;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Canonical email normalization. Must be used consistently everywhere so that
 * {@code User@Example.COM}, {@code user@example.com}, etc. resolve to one account.
 */
public final class EmailNormalizer {

    private EmailNormalizer() {
    }

    public static String normalize(String email) {
        if (email == null) {
            return null;
        }
        String normalized = Normalizer.normalize(email, Normalizer.Form.NFKC);
        return normalized.strip().toLowerCase(Locale.ROOT);
    }
}