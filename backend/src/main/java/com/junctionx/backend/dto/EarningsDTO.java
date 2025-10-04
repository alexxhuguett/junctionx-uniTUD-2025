package com.junctionx.backend.dto;

// Aided by LLM
public class EarningsDTO {

    public static class DayTotals {
        private int jobs;
        private double netEur;
        private double tipsEur;
        private int minutes;

        public DayTotals() {}
        public DayTotals(int jobs, double netEur, double tipsEur, int minutes) {
            this.jobs = jobs; this.netEur = netEur; this.tipsEur = tipsEur; this.minutes = minutes;
        }
        public int getJobs() { return jobs; }
        public double getNetEur() { return netEur; }
        public double getTipsEur() { return tipsEur; }
        public int getMinutes() { return minutes; }
        public void setJobs(int jobs) { this.jobs = jobs; }
        public void setNetEur(double netEur) { this.netEur = netEur; }
        public void setTipsEur(double tipsEur) { this.tipsEur = tipsEur; }
        public void setMinutes(int minutes) { this.minutes = minutes; }
    }

    public static class DayCompare {
        private String date;
        private String earnerId;
        private DayTotals actual;
        private DayTotals predicted;
        private double upliftPct;

        public DayCompare() {}
        public DayCompare(String date, String earnerId, DayTotals actual, DayTotals predicted, double upliftPct) {
            this.date = date; this.earnerId = earnerId; this.actual = actual; this.predicted = predicted; this.upliftPct = upliftPct;
        }
        public String getDate() { return date; }
        public String getEarnerId() { return earnerId; }
        public DayTotals getActual() { return actual; }
        public DayTotals getPredicted() { return predicted; }
        public double getUpliftPct() { return upliftPct; }
        public void setDate(String date) { this.date = date; }
        public void setEarnerId(String earnerId) { this.earnerId = earnerId; }
        public void setActual(DayTotals actual) { this.actual = actual; }
        public void setPredicted(DayTotals predicted) { this.predicted = predicted; }
        public void setUpliftPct(double upliftPct) { this.upliftPct = upliftPct; }
    }

    public static class RollingAvg {
        private String earnerId;
        private int windowDays;
        private double actualAvgPerDay;
        private double predictedAvgPerDay;
        private double upliftPct;

        public RollingAvg() {}
        public RollingAvg(String earnerId, int windowDays, double actualAvgPerDay, double predictedAvgPerDay, double upliftPct) {
            this.earnerId = earnerId; this.windowDays = windowDays; this.actualAvgPerDay = actualAvgPerDay;
            this.predictedAvgPerDay = predictedAvgPerDay; this.upliftPct = upliftPct;
        }
        public String getEarnerId() { return earnerId; }
        public int getWindowDays() { return windowDays; }
        public double getActualAvgPerDay() { return actualAvgPerDay; }
        public double getPredictedAvgPerDay() { return predictedAvgPerDay; }
        public double getUpliftPct() { return upliftPct; }
        public void setEarnerId(String earnerId) { this.earnerId = earnerId; }
        public void setWindowDays(int windowDays) { this.windowDays = windowDays; }
        public void setActualAvgPerDay(double v) { this.actualAvgPerDay = v; }
        public void setPredictedAvgPerDay(double v) { this.predictedAvgPerDay = v; }
        public void setUpliftPct(double upliftPct) { this.upliftPct = upliftPct; }
    }

    public static class RegionCompare {
        private String date;
        private int cityId;
        private double totalActual;
        private double totalPredicted;
        private double upliftPct;
        private int driversCount;

        public RegionCompare() {}
        public RegionCompare(String date, int cityId, double totalActual, double totalPredicted, double upliftPct, int driversCount) {
            this.date = date; this.cityId = cityId; this.totalActual = totalActual; this.totalPredicted = totalPredicted;
            this.upliftPct = upliftPct; this.driversCount = driversCount;
        }
        public String getDate() { return date; }
        public int getCityId() { return cityId; }
        public double getTotalActual() { return totalActual; }
        public double getTotalPredicted() { return totalPredicted; }
        public double getUpliftPct() { return upliftPct; }
        public int getDriversCount() { return driversCount; }
        public void setDate(String date) { this.date = date; }
        public void setCityId(int cityId) { this.cityId = cityId; }
        public void setTotalActual(double totalActual) { this.totalActual = totalActual; }
        public void setTotalPredicted(double totalPredicted) { this.totalPredicted = totalPredicted; }
        public void setUpliftPct(double upliftPct) { this.upliftPct = upliftPct; }
        public void setDriversCount(int driversCount) { this.driversCount = driversCount; }
    }
}
