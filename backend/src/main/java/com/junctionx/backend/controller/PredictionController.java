package com.junctionx.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api")
public class PredictionController {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String ML_API_BASE = "http://localhost:8000/prediction/";

    @GetMapping("/predictions")
    public List<Map<String, Object>> getPredictions(@RequestParam String ids) {
        List<String> rideIds = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        List<Map<String, Object>> results = new ArrayList<>();

        for (String id : rideIds) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", id);

            try {
                ResponseEntity<Map> response = restTemplate.getForEntity(ML_API_BASE + id, Map.class);
                Map<String, Object> mlResponse = response.getBody();

                if (mlResponse != null && mlResponse.containsKey("score")) {
                    entry.put("score", mlResponse.get("score"));
                } else {
                    entry.put("score", null);
                }

                // Optional: include the entire ML payload if FE wants raw info
                entry.put("ml_output", mlResponse);

            } catch (Exception e) {
                entry.put("score", null);
                entry.put("error", "ML API call failed: " + e.getMessage());
            }

            results.add(entry);
        }

        return results;
    }
}
