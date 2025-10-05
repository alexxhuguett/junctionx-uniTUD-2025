package com.junctionx.backend.sim.dto;

import java.util.List;

public record SimulationResult(
        BaselineMetrics baseline,
        SimMetrics simulated,
        List<TimelineEvent> timeline,
        List<String> notes
) {}
