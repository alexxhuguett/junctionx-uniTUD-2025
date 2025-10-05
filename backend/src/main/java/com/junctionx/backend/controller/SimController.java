package com.junctionx.backend.controller;

import com.junctionx.backend.dto.SimImprovements;
import com.junctionx.backend.dto.SimulateResponse;
import com.junctionx.backend.sim.SimulationService;
import com.junctionx.backend.sim.dto.SimulationResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/simulate")
public class SimController {

    private final SimulationService sim;

    public SimController(SimulationService sim) { this.sim = sim; }

    @GetMapping
    public SimulateResponse simulate(
            @RequestParam String driverId,
            @RequestParam String date,
            @RequestParam(required = false) Integer tol,
            @RequestParam(required = false) Integer lookahead,
            @RequestParam(required = false) Integer k
    ) {
        SimulationResult r = sim.simulateDay(driverId, LocalDate.parse(date), tol, lookahead, k);

        var b = r.baseline();
        var s = r.simulated();

        double dEarnings = s.earnings() - b.earnings();
        double dDrive    = s.driveMins() - b.driveMins();
        double dIdle     = s.idleMins()  - b.idleMins();
        double dRest     = s.restMins()  - b.restMins();
        int    dTrips    = s.tripsCount() - b.tripsCount();

        Double pEarnings = pct(dEarnings, b.earnings());
        Double pDrive    = pct(dDrive,    b.driveMins());
        Double pIdle     = pct(dIdle,     b.idleMins());
        Double pRest     = pct(dRest,     b.restMins());
        Double pTrips    = pct(dTrips,    b.tripsCount());

        var imp = new SimImprovements(
                dEarnings, pEarnings,
                dDrive,    pDrive,
                dIdle,     pIdle,
                dRest,     pRest,
                dTrips,    pTrips
        );

        return new SimulateResponse(b, s, imp, r.timeline(), r.notes());
    }

    private Double pct(double delta, double base) {
        if (Math.abs(base) < 1e-9) return null;
        return (delta / base) * 100.0;
    }
}
