package com.junctionx.backend.sim.ml;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ModelClient {

    private final RestTemplate http;
    private final String baseUrl;
    private final ConcurrentHashMap<String, Double> cache = new ConcurrentHashMap<>();

    public ModelClient(RestTemplateBuilder builder,
                       @Value("${ml.base-url:http://127.0.0.1:8000/prediction/}") String baseUrl) {
        this.http = builder
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(3))
                .build();
        // prefer IPv4 loopback to avoid the IPv6 first-attempt refusal log noise
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : (baseUrl + "/");
    }

    /** Returns a finite score; NEGATIVE_INFINITY if unavailable. */
    public double scoreRide(String rideId) {
        if (rideId == null || rideId.isBlank()) return Double.NEGATIVE_INFINITY;

        Double cached = cache.get(rideId);
        if (cached != null) return cached;

        try {
            ResponseEntity<Map> resp = http.getForEntity(baseUrl + rideId, Map.class);
            Map body = resp.getBody();
            double s = extractValue(body);

            if (Double.isFinite(s)) cache.putIfAbsent(rideId, s);
            return s;
        } catch (Exception e) {
            return Double.NEGATIVE_INFINITY;
        }
    }

    @SuppressWarnings("unchecked")
    private double extractValue(Map body) {
        if (body == null) return Double.NEGATIVE_INFINITY;

        // Accept either "rating" or "score" at top level
        double s = toDouble(body.get("rating"));
        if (Double.isFinite(s)) return s;

        s = toDouble(body.get("score"));
        if (Double.isFinite(s)) return s;

        // common nested shapes
        Object data = body.get("data");
        if (data instanceof Map<?,?> dm) {
            s = toDouble(((Map<String,Object>) dm).get("rating"));
            if (Double.isFinite(s)) return s;
            s = toDouble(((Map<String,Object>) dm).get("score"));
            if (Double.isFinite(s)) return s;
        }

        Object ml = body.get("ml_output");
        if (ml instanceof Map<?,?> mm) {
            s = toDouble(((Map<String,Object>) mm).get("rating"));
            if (Double.isFinite(s)) return s;
            s = toDouble(((Map<String,Object>) mm).get("score"));
            if (Double.isFinite(s)) return s;
        }

        return Double.NEGATIVE_INFINITY;
    }

    private double toDouble(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        if (v instanceof String s) {
            try { return Double.parseDouble(s); } catch (Exception ignore) {}
        }
        return Double.NEGATIVE_INFINITY;
    }

    public void clearCache() { cache.clear(); }
}
