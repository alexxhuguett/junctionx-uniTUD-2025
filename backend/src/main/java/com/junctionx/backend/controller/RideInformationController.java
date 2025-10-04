package com.junctionx.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

// Aided by LLM
@RestController
@RequestMapping("/api/rides")
public class RideInformationController {

    // Hardcoded feature rows matching the ML model's expected columns
    // See ml/tools/schema.py for the exact schema.
    private static final Map<String, Map<String, ? extends Serializable>> RIDES = Map.of(
            "00000000-0000-0000-0000-000000000000", Map.ofEntries(
                    entry("surge_multiplier", 1.3),
                    entry("distance_km", 4.7),
                    entry("duration_mins", 12.5),
                    entry("avg_speed_kmh", 22.6),
                    entry("active_minutes_since_rest", 180),
                    entry("hour", 17),
                    entry("weekday", 5),
                    entry("predicted_eph_drop", 18.2),
                    entry("cancellation_rate_drop", 0.04),
                    entry("is_ev", 1),
                    entry("experience_months", 26),
                    entry("driver_rating", 4.9),
                    entry("home_city_match", 1),
                    entry("city_id", 1),
                    entry("product", "UberX"),
                    entry("vehicle_type", "electric_sedan"),
                    entry("weather", "cloudy")
            ),
            "00000000-0000-0000-0000-000000000001", Map.ofEntries(
                    entry("surge_multiplier", 1.0),
                    entry("distance_km", 8.9),
                    entry("duration_mins", 22.4),
                    entry("avg_speed_kmh", 23.8),
                    entry("active_minutes_since_rest", 320),
                    entry("hour", 9),
                    entry("weekday", 2),
                    entry("predicted_eph_drop", 15.7),
                    entry("cancellation_rate_drop", 0.07),
                    entry("is_ev", 0),
                    entry("experience_months", 14),
                    entry("driver_rating", 4.6),
                    entry("home_city_match", 0),
                    entry("city_id", 2),
                    entry("product", "UberGreen"),
                    entry("vehicle_type", "hybrid_compact"),
                    entry("weather", "rainy")
            ),
            "00000000-0000-0000-0000-000000000002", Map.ofEntries(
                    entry("surge_multiplier", 2.1),
                    entry("distance_km", 17.3),
                    entry("duration_mins", 35.2),
                    entry("avg_speed_kmh", 29.5),
                    entry("active_minutes_since_rest", 410),
                    entry("hour", 22),
                    entry("weekday", 6),
                    entry("predicted_eph_drop", 21.8),
                    entry("cancellation_rate_drop", 0.02),
                    entry("is_ev", 0),
                    entry("experience_months", 48),
                    entry("driver_rating", 4.95),
                    entry("home_city_match", 1),
                    entry("city_id", 1),
                    entry("product", "UberBlack"),
                    entry("vehicle_type", "luxury_suv"),
                    entry("weather", "clear")
            ),
            "00000000-0000-0000-0000-000000000003", Map.ofEntries(
                    entry("surge_multiplier", 0.9),
                    entry("distance_km", 2.1),
                    entry("duration_mins", 6.4),
                    entry("avg_speed_kmh", 19.7),
                    entry("active_minutes_since_rest", 95),
                    entry("hour", 11),
                    entry("weekday", 1),
                    entry("predicted_eph_drop", 12.4),
                    entry("cancellation_rate_drop", 0.10),
                    entry("is_ev", 1),
                    entry("experience_months", 8),
                    entry("driver_rating", 4.3),
                    entry("home_city_match", 0),
                    entry("city_id", 3),
                    entry("product", "UberX"),
                    entry("vehicle_type", "compact_ev"),
                    entry("weather", "foggy")
            )
    );

    // Return the feature row for a given ride_id
    @GetMapping("/{ride_id}")
    public Map<String, ? extends Serializable> rideInfo(@PathVariable String ride_id) {
        var data = RIDES.get(ride_id);
        if (data == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ride_id not found");
        }
        return data;
    }

    // Return all available hardcoded ride_ids
    @GetMapping("")
    public List<String> listIds() {
        return new ArrayList<>(RIDES.keySet());
    }
}
