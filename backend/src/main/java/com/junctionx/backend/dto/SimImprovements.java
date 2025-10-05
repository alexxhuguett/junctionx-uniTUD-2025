package com.junctionx.backend.dto;

public record SimImprovements(
        double earningsAbs,  Double earningsPct,
        double driveMinsAbs, Double driveMinsPct,
        double idleMinsAbs,  Double idleMinsPct,
        double restMinsAbs,  Double restMinsPct,
        int tripsAbs,        Double tripsPct
) {}
