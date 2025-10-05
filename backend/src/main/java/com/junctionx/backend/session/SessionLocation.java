package com.junctionx.backend.session;

import java.time.LocalDateTime;

public class SessionLocation {
    private final double lat;
    private final double lon;
    private final String city;
    private final String hexId9;        // optional: align with your HeatMap hexagon_id_9
    private final LocalDateTime updatedAt;

    public SessionLocation(double lat, double lon, String city, String hexId9, LocalDateTime updatedAt) {
        this.lat = lat; this.lon = lon; this.city = city; this.hexId9 = hexId9; this.updatedAt = updatedAt;
    }
    public double getLat() { return lat; }
    public double getLon() { return lon; }
    public String getCity() { return city; }
    public String getHexId9() { return hexId9; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
