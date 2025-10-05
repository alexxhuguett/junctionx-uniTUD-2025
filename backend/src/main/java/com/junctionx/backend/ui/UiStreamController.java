package com.junctionx.backend.ui;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api") // you said all routes are under /api
public class UiStreamController {

    private final SseBus bus;

    public UiStreamController(SseBus bus) {
        this.bus = bus;
    }

    // FE is on http://localhost:5173
    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping(value = "/stream/decisions", produces = "text/event-stream")
    public SseEmitter stream() {
        return bus.register();
    }
}