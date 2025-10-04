package com.junctionx.backend.dto;

public class QualityDTO {
    private double acceptanceRate;        // 0..1
    private double onTimePct;             // 0..100
    private double cancelRate;            // 0..1
    private double avgPickupLatencyMins;  // minutes

    public QualityDTO() {}

    public QualityDTO(double acceptanceRate, double onTimePct, double cancelRate, double avgPickupLatencyMins) {
        this.acceptanceRate = acceptanceRate;
        this.onTimePct = onTimePct;
        this.cancelRate = cancelRate;
        this.avgPickupLatencyMins = avgPickupLatencyMins;
    }

    public double getAcceptanceRate() { return acceptanceRate; }
    public double getOnTimePct() { return onTimePct; }
    public double getCancelRate() { return cancelRate; }
    public double getAvgPickupLatencyMins() { return avgPickupLatencyMins; }

    public void setAcceptanceRate(double v) { this.acceptanceRate = v; }
    public void setOnTimePct(double v) { this.onTimePct = v; }
    public void setCancelRate(double v) { this.cancelRate = v; }
    public void setAvgPickupLatencyMins(double v) { this.avgPickupLatencyMins = v; }
}
