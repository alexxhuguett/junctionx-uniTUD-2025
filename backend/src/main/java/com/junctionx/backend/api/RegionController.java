package com.junctionx.backend.api;

import com.junctionx.backend.dto.EarningsDTO.RegionCompare;
import com.junctionx.backend.dto.GeoJson.FeatureCollection;
import com.junctionx.backend.service.EarningsService;
import com.junctionx.backend.service.RegionService;
import org.springframework.web.bind.annotation.*;

// Aided by LLM
@RestController
@RequestMapping("/api/region")
public class RegionController {

    private final RegionService regionService;
    private final EarningsService earningsService;

    public RegionController(RegionService regionService, EarningsService earningsService) {
        this.regionService = regionService;
        this.earningsService = earningsService;
    }

    @GetMapping("/{cityId}/drivers/trips")
    public FeatureCollection regionTrips(@PathVariable int cityId,
                                         @RequestParam String date,
                                         @RequestParam(required = false) Integer page,
                                         @RequestParam(required = false) Integer size,
                                         @RequestParam(required = false) String earnerIds) {
        return regionService.actualTrips(cityId, date, page, size, earnerIds);
    }

    @GetMapping("/{cityId}/drivers/trips/counterfactual")
    public FeatureCollection regionTripsCf(@PathVariable int cityId,
                                           @RequestParam String date,
                                           @RequestParam(required = false) Integer page,
                                           @RequestParam(required = false) Integer size,
                                           @RequestParam(required = false) String earnerIds) {
        return regionService.counterfactualTrips(cityId, date, page, size, earnerIds);
    }

    @GetMapping("/{cityId}/earnings/compare")
    public RegionCompare regionEarnings(@PathVariable int cityId,
                                        @RequestParam String date) {
        return earningsService.regionCompare(cityId, date);
    }

    @GetMapping("/{cityId}/heatmap")
    public FeatureCollection heatmap(@PathVariable int cityId,
                                     @RequestParam String date) {
        return regionService.heatmap(cityId, date);
    }
}
