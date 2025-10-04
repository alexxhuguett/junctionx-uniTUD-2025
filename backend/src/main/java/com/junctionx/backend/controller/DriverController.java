package com.junctionx.backend.controller;

import com.junctionx.backend.dto.GeoJson;
import com.junctionx.backend.service.DriverTripsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/driver")
public class DriverController {

    private final DriverTripsService driverTripsService;

    public DriverController(DriverTripsService driverTripsService) {
        this.driverTripsService = driverTripsService;
    }

    // GET /api/driver/{driverId}/trips?date=YYYY-MM-DD
    @GetMapping("/{driverId}/trips")
    public ResponseEntity<GeoJson.FeatureCollection> getTrips(
            @PathVariable String driverId,
            @RequestParam String date) {
        LocalDate d = LocalDate.parse(date);
        return ResponseEntity.ok(driverTripsService.getTrips(driverId, d));
    }

    // GET /api/driver/{driverId}/counterfactual-trips?date=YYYY-MM-DD
    @GetMapping("/{driverId}/counterfactual-trips")
    public ResponseEntity<GeoJson.FeatureCollection> getCounterfactualTrips(
            @PathVariable String driverId,
            @RequestParam String date) {
        LocalDate d = LocalDate.parse(date);
        return ResponseEntity.ok(driverTripsService.getCounterfactualTrips(driverId, d));
    }
}
