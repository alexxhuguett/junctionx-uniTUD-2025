package com.junctionx.backend.ui;

public record UiDecisionEvent(
        String type,         // "YES" | "NO" | "BREAK"
        String message,      // short human text to show in UI
        Route route,         // only for YES (otherwise null)
        String pickupTimeIso // ISO-8601, e.g. 2025-10-05T14:07:00Z
) {
    public record Route(Point pickup, Point dropoff) {}
    public record Point(double lat, double lon) {}
}