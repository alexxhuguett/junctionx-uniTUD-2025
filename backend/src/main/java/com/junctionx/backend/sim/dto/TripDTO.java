package com.junctionx.backend.sim.dto;

import java.time.OffsetDateTime;

public record TripDTO(
        String rideId,
        String driverId,
        String cityId,
        String pickupHexId9,
        String dropoffHexId9,
        OffsetDateTime startTs,
        OffsetDateTime endTs,
        Double durationMins,
        Double fare
) {}
