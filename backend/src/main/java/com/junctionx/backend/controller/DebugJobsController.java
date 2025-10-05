package com.junctionx.backend.controllers;

import com.junctionx.backend.model.repository.JobsReadRepository;
import com.junctionx.backend.sim.dto.TripDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/debug/jobs")
public class DebugJobsController {

    private final JobsReadRepository repo;

    public DebugJobsController(JobsReadRepository repo) { this.repo = repo; }

    @GetMapping("/driver-day")
    public List<TripDTO> driverDay(
            @RequestParam String driverId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dayStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dayEnd
    ) {
        return repo.findDriverTripsForDay(driverId, dayStart, dayEnd);
    }

    @GetMapping("/city-day")
    public List<TripDTO> cityDay(
            @RequestParam Integer cityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dayStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dayEnd
    ) {
        return repo.findCityTripsForDay(cityId, dayStart, dayEnd);
    }

    @GetMapping("/window")
    public List<TripDTO> window(
            @RequestParam Integer cityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fromTs,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime toTs,
            @RequestParam String pickupHexes // comma-separated
    ) {
        List<String> hexList = Arrays.stream(pickupHexes.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
        return repo.findWindowedCandidates(cityId, fromTs, toTs, hexList);
    }
}
