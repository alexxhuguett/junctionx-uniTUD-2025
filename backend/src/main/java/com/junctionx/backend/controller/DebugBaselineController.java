package com.junctionx.backend.controller;

import com.junctionx.backend.sim.BaselineService;
import com.junctionx.backend.sim.dto.BaselineMetrics;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/debug/baseline")
public class DebugBaselineController {
    private final BaselineService baseline;
    public DebugBaselineController(BaselineService baseline) { this.baseline = baseline; }

    @GetMapping
    public BaselineMetrics baseline(
            @RequestParam String driverId,
            @RequestParam String date  // YYYY-MM-DD
    ) {
        return baseline.compute(driverId, LocalDate.parse(date));
    }
}
