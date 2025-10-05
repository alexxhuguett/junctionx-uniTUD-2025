package com.junctionx.backend.sim;

import com.junctionx.backend.repository.JobsReadRepository;
import com.junctionx.backend.sim.dto.BaselineMetrics;
import com.junctionx.backend.sim.dto.TripDTO;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;

@Service
public class BaselineService {

    private static final double REST_THRESHOLD_MIN = 30.0;
    private static final ZoneId ZONE = ZoneId.of("Europe/Amsterdam");

    private final JobsReadRepository jobs;

    public BaselineService(JobsReadRepository jobs) { this.jobs = jobs; }

    public BaselineMetrics compute(String driverId, LocalDate date) {
        var dayStart = date.atStartOfDay(ZONE).toOffsetDateTime();
        var dayEnd   = date.plusDays(1).atStartOfDay(ZONE).toOffsetDateTime();

        List<TripDTO> trips = jobs.findDriverTripsForDay(driverId, dayStart, dayEnd);
        int tripsCount = trips.size();
        if (tripsCount == 0) {
            return new BaselineMetrics(0,0,0,0, null, dayStart, dayStart, null, 0);
        }

        double drive = 0, earn = 0, idle = 0, rest = 0;
        var first = trips.get(0);
        var last  = trips.get(tripsCount - 1);

        OffsetDateTime prevEnd = null;
        for (var t : trips) {
            drive += nz(t.durationMins());
            earn  += nz(t.fare());
            if (prevEnd != null) {
                long gap = Duration.between(prevEnd, t.startTs()).toMinutes();
                if (gap > 0) {
                    idle += gap;
                    if (gap >= REST_THRESHOLD_MIN) rest += gap;
                }
            }
            prevEnd = t.endTs();
        }

        return new BaselineMetrics(
                drive, earn, idle, rest,
                first.cityId(), first.startTs(), last.endTs(), first.pickupHexId9(),
                tripsCount
        );
    }

    private double nz(Double v) { return v == null ? 0.0 : v; }
}
