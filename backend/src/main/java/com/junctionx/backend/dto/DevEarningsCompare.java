package com.junctionx.backend.dto;

public class DevEarningsCompare {
    private double actualTotalEur;
    private double predictedTotalEur;
    private double upliftEur;
    private double upliftPct; // 0..100

    public DevEarningsCompare() {}

    public DevEarningsCompare(double actualTotalEur, double predictedTotalEur) {
        this.actualTotalEur = actualTotalEur;
        this.predictedTotalEur = predictedTotalEur;
        this.upliftEur = predictedTotalEur - actualTotalEur;
        this.upliftPct = actualTotalEur == 0 ? 0.0 : (upliftEur / actualTotalEur) * 100.0;
    }

    public double getActualTotalEur() { return actualTotalEur; }
    public double getPredictedTotalEur() { return predictedTotalEur; }
    public double getUpliftEur() { return upliftEur; }
    public double getUpliftPct() { return upliftPct; }

    public void setActualTotalEur(double v) { this.actualTotalEur = v; }
    public void setPredictedTotalEur(double v) { this.predictedTotalEur = v; }
    public void setUpliftEur(double v) { this.upliftEur = v; }
    public void setUpliftPct(double v) { this.upliftPct = v; }
}
