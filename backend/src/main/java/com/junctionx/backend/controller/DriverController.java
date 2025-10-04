package com.junctionx.backend.controller;

import com.junctionx.backend.dto.EarningsDTO.DayCompare;
import com.junctionx.backend.dto.EarningsDTO.RollingAvg;
import com.junctionx.backend.dto.GeoJson;
import com.junctionx.backend.dto.QualityDTO;
import com.junctionx.backend.dto.RecommendationDTO;
import com.junctionx.backend.service.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/driver")
public class DriverController {

    private final DriverTripsService driverTripsService;
    private final EarningsService earningsService;
    private final RecommendationService recommendationService;
    private final QualityService qualityService;

    public DriverController(DriverTripsService driverTripsService,
                            EarningsService earningsService,
                            RecommendationService recommendationService,
                            QualityService qualityService) {
        this.driverTripsService = driverTripsService;
        this.earningsService = earningsService;
        this.recommendationService = recommendationService;
        this.qualityService = qualityService;
    }

    // ✅ Trips — actual
    @GetMapping("/{earnerId}/trips")
    public GeoJson.FeatureCollection driverTrips(@PathVariable String earnerId,
                                                 @RequestParam String date,
                                                 @RequestParam(required = false) Integer page,
                                                 @RequestParam(required = false) Integer size) {
        return driverTripsService.actualTrips(earnerId, date, page, size);
    }

    // ✅ Trips — counterfactual
    @GetMapping("/{earnerId}/trips/counterfactual")
    public GeoJson.FeatureCollection driverTripsCf(@PathVariable String earnerId,
                                                   @RequestParam String date,
                                                   @RequestParam(required = false) Integer page,
                                                   @RequestParam(required = false) Integer size) {
        return driverTripsService.counterfactualTrips(earnerId, date, page, size);
    }

    // ✅ Earnings — day comparison
    @GetMapping("/{earnerId}/earnings/day")
    public DayCompare earningsDay(@PathVariable String earnerId,
                                  @RequestParam String date) {
        return earningsService.dayCompare(earnerId, date);
    }

    // ✅ Earnings — rolling average
    @GetMapping("/{earnerId}/earnings/avg")
    public RollingAvg earningsAvg(@PathVariable String earnerId,
                                  @RequestParam(defaultValue = "28") Integer windowDays) {
        return earningsService.rollingAvg(earnerId, windowDays);
    }

    // ✅ Recommendations
    @GetMapping("/{earnerId}/recommendations")
    public List<RecommendationDTO> recommendations(@PathVariable String earnerId,
                                                   @RequestParam String date,
                                                   @RequestParam(required = false) String now) {
        return recommendationService.recommendations(earnerId, date, now);
    }

    // ✅ Quality
    @GetMapping("/{earnerId}/quality")
    public QualityDTO quality(@PathVariable String earnerId,
                              @RequestParam String date) {
        return qualityService.quality(earnerId, date);
    }
}
