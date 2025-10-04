package com.junctionx.backend.dto;

public class DevDriverSummary {
    private String earnerId;
    private int trips;
    private double actualGrossEur;
    private double predictedGrossEur;

    public DevDriverSummary() {}

    public DevDriverSummary(String earnerId, int trips, double actualGrossEur, double predictedGrossEur) {
        this.earnerId = earnerId;
        this.trips = trips;
        this.actualGrossEur = actualGrossEur;
        this.predictedGrossEur = predictedGrossEur;
    }

    public String getEarnerId() { return earnerId; }
    public int getTrips() { return trips; }
    public double getActualGrossEur() { return actualGrossEur; }
    public double getPredictedGrossEur() { return predictedGrossEur; }

    public void setEarnerId(String earnerId) { this.earnerId = earnerId; }
    public void setTrips(int trips) { this.trips = trips; }
    public void setActualGrossEur(double actualGrossEur) { this.actualGrossEur = actualGrossEur; }
    public void setPredictedGrossEur(double predictedGrossEur) { this.predictedGrossEur = predictedGrossEur; }
}
