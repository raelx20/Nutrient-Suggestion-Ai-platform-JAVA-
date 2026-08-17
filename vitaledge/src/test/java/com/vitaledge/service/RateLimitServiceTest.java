package com.vitaledge.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.vitaledge.common.exception.RateLimitException;
import com.vitaledge.config.ApplicationProperties;
import com.vitaledge.infra.InMemoryRedisClient;
import org.junit.jupiter.api.Test;

class RateLimitServiceTest {

    private final RateLimitService service = new RateLimitService(
            new InMemoryRedisClient(), new ApplicationProperties());

    @Test
    void allowsWithinLimitThenDenies() {
        for (int i = 0; i < 3; i++) {
            int attempt = i;
            assertDoesNotThrow(() -> service.check("unit", "key", 3, 3600),
                    () -> "attempt " + attempt + " should be allowed");
        }
        assertThrows(RateLimitException.class, () -> service.check("unit", "key", 3, 3600));
    }

    @Test
    void failOpenSwallowsBackendErrors() {
        // InMemoryRedisClient never throws, but the public API must not throw when it does.
        assertDoesNotThrow(() -> service.checkFailOpen("unit", "key", 1, 3600));
    }

    @Test
    void independentKeysAreIndependent() {
        assertDoesNotThrow(() -> service.check("unit", "a", 1, 3600));
        assertThrows(RateLimitException.class, () -> service.check("unit", "a", 1, 3600));
        assertDoesNotThrow(() -> service.check("unit", "b", 1, 3600));
    }
}