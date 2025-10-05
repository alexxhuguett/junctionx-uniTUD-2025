package com.junctionx.backend.controller;

import com.junctionx.backend.sim.ml.ModelClient;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/debug/ml")
public class DebugMlController {

    private final ModelClient client;

    public DebugMlController(ModelClient client) { this.client = client; }

    @GetMapping("/score")
    public List<Map<String, Object>> score(@RequestParam String ids) {
        List<String> rideIds = Arrays.stream(ids.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();

        List<Map<String, Object>> out = new ArrayList<>();
        for (String id : rideIds) {
            double s = client.scoreRide(id);
            out.add(Map.of("id", id, "score", s));
        }
        return out;
    }

    @PostMapping("/clear-cache")
    public Map<String, Object> clearCache() {
        client.clearCache();
        return Map.of("ok", true);
    }
}
