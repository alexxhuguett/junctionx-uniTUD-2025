package com.junctionx.backend.sim.dto;

import java.time.OffsetDateTime;

public record BaselineMetrics(
        double driveMins,
        double earnings,
        double idleMins,
        double restMins,
        String cityId,
        OffsetDateTime shiftStart,
        OffsetDateTime shiftEnd,
        String startHex,
        int tripsCount   // NEW
) {}
