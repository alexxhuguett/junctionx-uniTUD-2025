package com.junctionx.backend.ui;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dev/decision")
@CrossOrigin(origins = "http://localhost:5173") // FE dev server
public class DevDecisionTestController {

    private final SseBus bus;

    public DevDecisionTestController(SseBus bus) {
        this.bus = bus;
    }

    /** Push a YES decision with a simple pickup→dropoff route and now as pickup time */
    @PostMapping("/yes")
    public void yes() {
        UiDecisionEvent ev = new UiDecisionEvent(
                "YES",
                "Take it. Fastest via NE.",
                new UiDecisionEvent.Route(
                        new UiDecisionEvent.Point(51.999, 4.370), // pickup (Delft-ish)
                        new UiDecisionEvent.Point(52.005, 4.380)  // dropoff
                ),
                java.time.OffsetDateTime.now().toString()
        );
        bus.push(ev);
    }

    /** Push a NO decision */
    @PostMapping("/no")
    public void noDecision() {
        UiDecisionEvent ev = new UiDecisionEvent(
                "NO",
                "Low demand. Wait or reposition.",
                null,
                java.time.OffsetDateTime.now().toString()
        );
        bus.push(ev);
    }

    /** Push a BREAK decision */
    @PostMapping("/break")
    public void breakDecision() {
        UiDecisionEvent ev = new UiDecisionEvent(
                "BREAK",
                "Take a 10 min break.",
                null,
                java.time.OffsetDateTime.now().toString()
        );
        bus.push(ev);
    }
}