package com.junctionx.backend.dto;

/**
 * Container DTO with nested types for earnings-related responses.
 */
public class EarningsDTO {

    // ---- day-level compare for a single earner ----
    public static class DayCompare {
        private String date;            // ISO yyyy-MM-dd
        private double actualGrossEur;
        private double predictedGrossEur;
        private double deltaEur;        // predicted - actual
        private double deltaPct;        // delta / actual * 100

        public DayCompare() {}

        public DayCompare(String date, double actualGrossEur, double predictedGrossEur) {
            this.date = date;
            this.actualGrossEur = actualGrossEur;
            this.predictedGrossEur = predictedGrossEur;
            this.deltaEur = predictedGrossEur - actualGrossEur;
            this.deltaPct = actualGrossEur == 0 ? 0 : (this.deltaEur / actualGrossEur) * 100.0;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public double getActualGrossEur() { return actualGrossEur; }
        public void setActualGrossEur(double actualGrossEur) { this.actualGrossEur = actualGrossEur; }

        public double getPredictedGrossEur() { return predictedGrossEur; }
        public void setPredictedGrossEur(double predictedGrossEur) { this.predictedGrossEur = predictedGrossEur; }

        public double getDeltaEur() { return deltaEur; }
        public void setDeltaEur(double deltaEur) { this.deltaEur = deltaEur; }

        public double getDeltaPct() { return deltaPct; }
        public void setDeltaPct(double deltaPct) { this.deltaPct = deltaPct; }
    }

    // ---- rolling average summary for a single earner ----
    public static class RollingAvg {
        private int windowDays;                 // e.g. 28
        private double actualAvgGrossPerDay;
        private double predictedAvgGrossPerDay;
        private double deltaEurPerDay;          // predicted - actual
        private double deltaPctPerDay;          // delta / actual * 100

        public RollingAvg() {}

        public RollingAvg(int windowDays, double actualAvgGrossPerDay, double predictedAvgGrossPerDay) {
            this.windowDays = windowDays;
            this.actualAvgGrossPerDay = actualAvgGrossPerDay;
            this.predictedAvgGrossPerDay = predictedAvgGrossPerDay;
            this.deltaEurPerDay = predictedAvgGrossPerDay - actualAvgGrossPerDay;
            this.deltaPctPerDay = actualAvgGrossPerDay == 0 ? 0 : (this.deltaEurPerDay / actualAvgGrossPerDay) * 100.0;
        }

        public int getWindowDays() { return windowDays; }
        public void setWindowDays(int windowDays) { this.windowDays = windowDays; }

        public double getActualAvgGrossPerDay() { return actualAvgGrossPerDay; }
        public void setActualAvgGrossPerDay(double v) { this.actualAvgGrossPerDay = v; }

        public double getPredictedAvgGrossPerDay() { return predictedAvgGrossPerDay; }
        public void setPredictedAvgGrossPerDay(double v) { this.predictedAvgGrossPerDay = v; }

        public double getDeltaEurPerDay() { return deltaEurPerDay; }
        public void setDeltaEurPerDay(double v) { this.deltaEurPerDay = v; }

        public double getDeltaPctPerDay() { return deltaPctPerDay; }
        public void setDeltaPctPerDay(double v) { this.deltaPctPerDay = v; }
    }

    // ---- region aggregate compare (already referenced by RegionController) ----
    public static class RegionCompare {
        private double actualTotalEur;
        private double predictedTotalEur;
        private double upliftEur;               // predicted - actual
        private double upliftPct;               // uplift / actual * 100

        public RegionCompare() {}

        public RegionCompare(double actualTotalEur, double predictedTotalEur) {
            this.actualTotalEur = actualTotalEur;
            this.predictedTotalEur = predictedTotalEur;
            this.upliftEur = predictedTotalEur - actualTotalEur;
            this.upliftPct = actualTotalEur == 0 ? 0 : (upliftEur / actualTotalEur) * 100.0;
        }

        public double getActualTotalEur() { return actualTotalEur; }
        public void setActualTotalEur(double v) { this.actualTotalEur = v; }

        public double getPredictedTotalEur() { return predictedTotalEur; }
        public void setPredictedTotalEur(double v) { this.predictedTotalEur = v; }

        public double getUpliftEur() { return upliftEur; }
        public void setUpliftEur(double v) { this.upliftEur = v; }

        public double getUpliftPct() { return upliftPct; }
        public void setUpliftPct(double v) { this.upliftPct = v; }
    }
}
