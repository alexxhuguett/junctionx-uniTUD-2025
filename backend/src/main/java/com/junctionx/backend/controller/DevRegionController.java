package com.junctionx.backend.controller;

import com.junctionx.backend.dto.DevDriverSummary;
import com.junctionx.backend.dto.DevEarningsCompare;
import com.junctionx.backend.dto.GeoJson;
import com.junctionx.backend.service.DevRegionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dev/region")
public class DevRegionController {

    private final DevRegionService devRegionService;

    public DevRegionController(DevRegionService devRegionService) {
        this.devRegionService = devRegionService;
    }

    @GetMapping("/{regionId}/drivers")
    public List<DevDriverSummary> drivers(@PathVariable String regionId,
                                          @RequestParam String date) {
        return devRegionService.activeDrivers(regionId, date);
    }

    @GetMapping("/{regionId}/trips/actual")
    public GeoJson.FeatureCollection regionActualTrips(@PathVariable String regionId,
                                                       @RequestParam String date) {
        return devRegionService.actualTrips(regionId, date);
    }

    @GetMapping("/{regionId}/trips/counterfactual")
    public GeoJson.FeatureCollection regionCounterfactualTrips(@PathVariable String regionId,
                                                               @RequestParam String date) {
        return devRegionService.counterfactualTrips(regionId, date);
    }

    @GetMapping("/{regionId}/earnings/compare")
    public DevEarningsCompare earningsCompare(@PathVariable String regionId,
                                              @RequestParam String date) {
        return devRegionService.earningsCompare(regionId, date);
    }
}
