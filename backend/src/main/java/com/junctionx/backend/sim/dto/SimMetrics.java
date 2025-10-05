package com.junctionx.backend.sim.dto;

public record SimMetrics(
        double driveMins,
        double earnings,
        double idleMins,
        double restMins,
        int tripsCount
) {}
