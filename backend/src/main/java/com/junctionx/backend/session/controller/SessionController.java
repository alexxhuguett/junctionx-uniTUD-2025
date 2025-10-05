package com.junctionx.backend.session.controller;

import com.junctionx.backend.session.UserSession;
import com.junctionx.backend.session.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// com.junctionx.backend.session.controller.SessionController
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessions;

    public SessionController(SessionService sessions) { this.sessions = sessions; }

    @PostMapping("/{earnerId}/start")
    public ResponseEntity<UserSession> start(@PathVariable String earnerId) {
        return ResponseEntity.ok(sessions.startSession(earnerId));
    }

    @GetMapping("/{earnerId}")
    public ResponseEntity<UserSession> get(@PathVariable String earnerId) {
        return ResponseEntity.ok(sessions.getOrThrow(earnerId));
    }

    @PostMapping("/{earnerId}/end")
    public ResponseEntity<Void> end(@PathVariable String earnerId) {
        sessions.endSession(earnerId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{earnerId}/location")
    public ResponseEntity<Void> location(@PathVariable String earnerId,
                                         @RequestParam double lat,
                                         @RequestParam double lon,
                                         @RequestParam String city,
                                         @RequestParam(required = false) String hexId9) {
        sessions.updateLocation(earnerId, lat, lon, city, hexId9);
        return ResponseEntity.noContent().build();
    }

    // --- Break toggle API for your button ---
    @PostMapping("/{earnerId}/break/toggle")
    public ResponseEntity<Void> toggle(@PathVariable String earnerId) {
        sessions.toggleBreak(earnerId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{earnerId}/break/start")
    public ResponseEntity<Void> startBreak(@PathVariable String earnerId) {
        sessions.startBreak(earnerId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{earnerId}/break/end")
    public ResponseEntity<Void> endBreak(@PathVariable String earnerId) {
        sessions.endBreak(earnerId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{earnerId}/jobs/{jobId}/complete")
    public ResponseEntity<Void> complete(@PathVariable String earnerId, @PathVariable String jobId) {
        sessions.recordJob(earnerId, jobId);
        return ResponseEntity.noContent().build();
    }
}
