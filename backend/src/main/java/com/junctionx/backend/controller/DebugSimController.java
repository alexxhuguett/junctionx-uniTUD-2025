package com.junctionx.backend.controller;

import com.junctionx.backend.sim.SimulationService;
import com.junctionx.backend.sim.dto.SimulationResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/debug/sim")
public class DebugSimController {

    private final SimulationService sim;

    public DebugSimController(SimulationService sim) {
        this.sim = sim;
    }

    @GetMapping
    public SimulationResult run(
            @RequestParam String driverId,
            @RequestParam String date, // YYYY-MM-DD
            @RequestParam(required = false) Integer tol,
            @RequestParam(required = false) Integer lookahead,
            @RequestParam(required = false) Integer k
    ) {
        return sim.simulateDay(driverId, LocalDate.parse(date), tol, lookahead, k);
    }
}
