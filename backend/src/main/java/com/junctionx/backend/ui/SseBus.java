package com.junctionx.backend.ui;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseBus {
    private final Set<SseEmitter> clients = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public SseEmitter register() {
        SseEmitter emitter = new SseEmitter(0L); // no timeout
        clients.add(emitter);
        emitter.onCompletion(() -> clients.remove(emitter));
        emitter.onTimeout(() -> clients.remove(emitter));
        return emitter;
    }

    /** Sends a single "decision" event with the given payload to all clients. */
    public void push(Object payload) {
        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter c : clients) {
            try {
                c.send(SseEmitter.event().name("decision").data(payload));
            } catch (Exception ex) {
                dead.add(c);
            }
        }
        clients.removeAll(dead);
    }
}