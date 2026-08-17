package com.vitaledge.web.controller;

import com.vitaledge.service.CacheService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final CacheService cacheService;

    public HealthController(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping("/health/live")
    public ResponseEntity<Map<String, Object>> liveness() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "vitaledge-api"));
    }

    @GetMapping("/health/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        boolean cacheUp = cacheService.ping();
        Map<String, Object> body = Map.of(
                "status", cacheUp ? "UP" : "DEGRADED",
                "service", "vitaledge-api",
                "checks", Map.of("redis", cacheUp ? "UP" : "DOWN"));
        return cacheUp ? ResponseEntity.ok(body) : ResponseEntity.status(503).body(body);
    }
}