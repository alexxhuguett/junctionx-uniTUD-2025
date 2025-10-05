package com.junctionx.backend.sim.dto;

import java.time.OffsetDateTime;

public record TimelineEvent(
        String type,
        OffsetDateTime start,
        OffsetDateTime end,
        String fromHex,
        String toHex,
        String rideId,
        Double earnings
) {}
