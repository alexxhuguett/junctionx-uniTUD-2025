package com.junctionx.backend.session;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserSession {

    // --- Identity ---
    private final String earnerId;

    // --- Lifecycle ---
    private final LocalDateTime startedAt;
    private Optional<LocalDateTime> finishedAt = Optional.empty();

    // --- Breaks (toggle-based) ---
    private boolean breakActive = false;
    private LocalDateTime breakStartedAt;     // when current break started (null if none)
    private Duration totalBreak = Duration.ZERO;
    private LocalDateTime lastBreakEndedAt;   // for continuous-driving calculation

    // --- Work stats ---
    private int jobsDone = 0;
    private final List<String> jobIds = new ArrayList<>();

    // --- Money (separated) ---
    private double earningsFromJobs = 0.0;    // ONLY from jobs
    private double bonusAccruedEur = 0.0;     // ONLY from incentives/bonuses (update elsewhere)
    private int bonusJobsCounted = 0;         // jobs contributing to the weekly target

    // --- Location ---
    private SessionLocation lastLocation;

    public UserSession(String earnerId) {
        this.earnerId = earnerId;
        this.startedAt = LocalDateTime.now();
        this.lastBreakEndedAt = this.startedAt;
    }

    // ===== getters =====
    public String getEarnerId() { return earnerId; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public Optional<LocalDateTime> getFinishedAt() { return finishedAt; }

    public boolean isBreakActive() { return breakActive; }
    public LocalDateTime getBreakStartedAt() { return breakStartedAt; }
    public Duration getTotalBreak() { return totalBreak; }
    public LocalDateTime getLastBreakEndedAt() { return lastBreakEndedAt; }

    public int getJobsDone() { return jobsDone; }
    public List<String> getJobIds() { return jobIds; }

    public double getEarningsFromJobs() { return earningsFromJobs; }
    public double getBonusAccruedEur() { return bonusAccruedEur; }
    public int getBonusJobsCounted() { return bonusJobsCounted; }

    public SessionLocation getLastLocation() { return lastLocation; }

    // ===== derived times =====
    public Duration getElapsed() {
        return Duration.between(startedAt, finishedAt.orElse(LocalDateTime.now()));
    }

    public Duration getActiveWorkTime() {
        Duration breakSoFar = totalBreak;
        if (breakActive && breakStartedAt != null) {
            breakSoFar = breakSoFar.plus(Duration.between(breakStartedAt, LocalDateTime.now()));
        }
        return getElapsed().minus(breakSoFar);
    }

    /** Time driving continuously since the last break ended (or session start). */
    public Duration getContinuousDriving() {
        LocalDateTime anchor = (lastBreakEndedAt != null) ? lastBreakEndedAt : startedAt;
        // If currently on break, continuous driving is zero.
        if (breakActive) return Duration.ZERO;
        return Duration.between(anchor, LocalDateTime.now());
    }

    // ===== commands =====
    public void startBreak() {
        if (breakActive) return;
        breakActive = true;
        breakStartedAt = LocalDateTime.now();
    }

    public void endBreak() {
        if (!breakActive) return;
        LocalDateTime now = LocalDateTime.now();
        if (breakStartedAt != null) {
            totalBreak = totalBreak.plus(Duration.between(breakStartedAt, now));
        }
        breakActive = false;
        breakStartedAt = null;
        lastBreakEndedAt = now;
    }

    public void toggleBreak() { if (breakActive) endBreak(); else startBreak(); }

    public void recordJob(String jobId, double jobEarnings) {
        jobsDone += 1;
        jobIds.add(jobId);
        earningsFromJobs += (jobEarnings > 0 ? jobEarnings : 0.0);
        bonusJobsCounted += 1; // per your rule: all jobs count toward bonus
    }

    public void addBonus(double amountEur) {
        bonusAccruedEur += Math.max(0.0, amountEur);
    }

    public void updateLocation(SessionLocation location) { this.lastLocation = location; }

    public void finishNow() { this.finishedAt = Optional.of(LocalDateTime.now()); }
}
