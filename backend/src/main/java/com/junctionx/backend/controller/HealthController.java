package com.junctionx.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

@RestController
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        try (Connection conn = dataSource.getConnection()) {
            boolean valid = conn.isValid(2);
            return Map.of("status", "ok", "db", valid ? "up" : "down");
        } catch (Exception e) {
            return Map.of("status", "error", "db", "down", "error", e.getMessage());
        }
    }
}
