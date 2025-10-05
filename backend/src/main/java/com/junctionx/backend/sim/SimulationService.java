package com.junctionx.backend.sim;

import com.junctionx.backend.repository.JobsReadRepository;
import com.junctionx.backend.sim.dto.BaselineMetrics;
import com.junctionx.backend.sim.dto.SimMetrics;
import com.junctionx.backend.sim.dto.SimulationResult;
import com.junctionx.backend.sim.dto.TimelineEvent;
import com.junctionx.backend.sim.dto.TripDTO;
import com.junctionx.backend.sim.ml.ModelClient;
import com.junctionx.backend.sim.util.H3Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SimulationService {

    private final JobsReadRepository jobs;
    private final BaselineService baselineService;
    private final ModelClient model;
    private final H3Util h3;

    private final int lookaheadMinutesDefault;
    private final int toleranceMinutesDefault;
    private final int hexRingKDefault;

    private static final double REST_THRESHOLD_MIN = 30.0;
    private static final ZoneId ZONE = ZoneId.of("Europe/Amsterdam");

    public SimulationService(JobsReadRepository jobs,
                             BaselineService baselineService,
                             ModelClient model,
                             H3Util h3,
                             @Value("${simulation.lookahead-minutes:30}") int lookahead,
                             @Value("${simulation.tolerance-minutes:5}") int tolerance,
                             @Value("${simulation.hex-ring-k:2}") int hexK) {
        this.jobs = jobs;
        this.baselineService = baselineService;
        this.model = model;
        this.h3 = h3;
        this.lookaheadMinutesDefault = lookahead;
        this.toleranceMinutesDefault = tolerance;
        this.hexRingKDefault = hexK;
    }

    /** Build an alternative day that reaches ~ the same drive minutes as baseline. */
    public SimulationResult simulateDay(String driverId,
                                        LocalDate date,
                                        Integer toleranceMinutes,
                                        Integer lookaheadMinutes,
                                        Integer hexRingK) {

        // 1) Baseline targets/state
        BaselineMetrics base = baselineService.compute(driverId, date);
        double targetDrive = base.driveMins();
        if (base.cityId() == null || base.startHex() == null || targetDrive <= 0) {
            return new SimulationResult(base, new SimMetrics(0,0,0,0,0), List.of(), List.of("No baseline trips."));
        }

        // --- Clamp tolerance so we don't "early-stop" at time 0 when tol >= targetDrive
        final int requestedTol = Optional.ofNullable(toleranceMinutes).orElse(toleranceMinutesDefault);
        final int tolMax = Math.max(0, (int) Math.floor(Math.max(0.0, targetDrive - 1.0))); // e.g., target 8 -> tolMax 7
        final int tol = Math.min(Math.max(0, requestedTol), tolMax);

        final int laMin = Optional.ofNullable(lookaheadMinutes).orElse(lookaheadMinutesDefault);
        final int k     = Optional.ofNullable(hexRingK).orElse(hexRingKDefault);

        // 2) Sim state
        OffsetDateTime time = base.shiftStart();
        String currHex = base.startHex();
        int cityIdInt = Integer.parseInt(base.cityId());

        double drive=0, earn=0, idle=0, rest=0;
        int tripsCount = 0;

        List<TimelineEvent> timeline = new ArrayList<>();
        List<String> notes = new ArrayList<>();
        Set<String> consumed = new HashSet<>();

        if (requestedTol > tol) {
            notes.add("Tolerance clamped from " + requestedTol + " to " + tol + " (baseline drive " + targetDrive + " min).");
        }

        // 3) Main loop — stop when within tolerance
        while (drive < (targetDrive - tol)) {
            OffsetDateTime windowEnd = time.plusMinutes(laMin);

            // Spatial filter via H3 k-ring
            List<String> pickupSet = h3.kRings(currHex, k);

            // Time+space candidates; never reuse consumed
            List<TripDTO> cand = jobs.findWindowedCandidates(cityIdInt, time, windowEnd, pickupSet)
                    .stream()
                    .filter(t -> !consumed.contains(t.rideId()))
                    .collect(Collectors.toList());

            if (cand.isEmpty()) {
                // Nothing nearby in this window → move time forward a bit
                OffsetDateTime next = time.plusMinutes(5);
                if (next.isAfter(base.shiftEnd().plusHours(2))) {
                    notes.add("Bailed: time moved beyond shiftEnd + 2h.");
                    break;
                }
                long gapA = addIdleEvent(timeline, currHex, time, next);
                idle += gapA;
                if (gapA >= REST_THRESHOLD_MIN) rest += gapA;
                time = next;
                continue;
            }

            // Score each candidate one-by-one and pick the highest
            TripDTO best = cand.get(0);
            double bestScore = Double.NEGATIVE_INFINITY;
            for (TripDTO t : cand) {
                double s = model.scoreRide(t.rideId()); // reads "rating" or "score"
                if (s > bestScore) { bestScore = s; best = t; }
            }
            if (!Double.isFinite(bestScore)) {
                notes.add("Scores unavailable; fell back to earliest candidate.");
            }

            // Pre-trip idle gap
            if (best.startTs().isAfter(time)) {
                long gapB = addIdleEvent(timeline, currHex, time, best.startTs());
                idle += gapB;
                if (gapB >= REST_THRESHOLD_MIN) rest += gapB;
            }

            // Take the trip
            double d = nz(best.durationMins());
            double e = nz(best.fare());
            drive += d;
            earn  += e;
            tripsCount += 1;

            timeline.add(new TimelineEvent("trip",
                    best.startTs(), best.endTs(),
                    best.pickupHexId9(), best.dropoffHexId9(),
                    best.rideId(), e));

            // Update state
            time = best.endTs();
            currHex = best.dropoffHexId9();
            consumed.add(best.rideId());

            // Overshoot guard: if we exceed target + tol, revert last trip and nudge time
            if (drive > (targetDrive + tol)) {
                TimelineEvent last = timeline.remove(timeline.size()-1);
                drive -= d; earn -= e; tripsCount -= 1;
                time = last.start();
                currHex = last.fromHex();
                consumed.remove(best.rideId());
                notes.add("Rejected last trip (overshoot beyond tolerance).");
                OffsetDateTime next = time.plusMinutes(5);
                long gapC = addIdleEvent(timeline, currHex, time, next);
                idle += gapC;
                if (gapC >= REST_THRESHOLD_MIN) rest += gapC;
                time = next;
            }
        }

        SimMetrics sim = new SimMetrics(drive, earn, idle, rest, tripsCount);
        return new SimulationResult(base, sim, timeline, notes);
    }

    /** Adds an idle/rest event and returns the gap minutes so caller can update metrics. */
    private long addIdleEvent(List<TimelineEvent> timeline,
                              String hex,
                              OffsetDateTime from,
                              OffsetDateTime to) {
        long gap = Duration.between(from, to).toMinutes();
        if (gap > 0) {
            boolean isRest = gap >= REST_THRESHOLD_MIN;
            timeline.add(new TimelineEvent(isRest ? "rest" : "idle",
                    from, to, hex, hex, null, null));
        }
        return gap;
    }

    private double nz(Double v) { return v == null ? 0.0 : v; }
}
