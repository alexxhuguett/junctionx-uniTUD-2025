package com.junctionx.backend.dto;

import com.junctionx.backend.sim.dto.BaselineMetrics;
import com.junctionx.backend.sim.dto.SimMetrics;
import com.junctionx.backend.sim.dto.TimelineEvent;

import java.util.List;

public record SimulateResponse(
        BaselineMetrics baseline,
        SimMetrics simulated,
        SimImprovements improvements,
        List<TimelineEvent> timeline,
        List<String> notes
) {}
