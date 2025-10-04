package com.junctionx.backend.model;

import jakarta.persistence.*;

/**
 * Weekly incentive (quest/bonus) for a driver or courier (earner).
 * Schema: earner_id, city_id, week, target_jobs, completed_jobs, bonus_eur
 */
@Entity
@Table(
        name = "incentives_weekly",
        indexes = {
                @Index(name = "idx_incentive_driver_week", columnList = "earner_id,week"),
                @Index(name = "idx_incentive_city", columnList = "city_id")
        }
)
public class IncentiveWeekly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "incentive_id")
    private Long id;

    /** The driver/courier who the incentive belongs to. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "earner_id", nullable = false)
    private Earner earner;

    /** City identifier (integer, no foreign key table). */
    @Column(name = "city_id", nullable = false)
    private Integer cityId;

    /** ISO week string, e.g. "2025-W40". */
    @Column(name = "week", length = 10, nullable = false)
    private String week;

    /** Number of jobs needed to unlock the bonus. */
    @Column(name = "target_jobs", nullable = false)
    private Integer targetJobs;

    /** Number of jobs completed that week. */
    @Column(name = "completed_jobs", nullable = false)
    private Integer completedJobs;

    /** Bonus amount (in euros). */
    @Column(name = "bonus_eur")
    private Double bonusEur;

    // ========= Constructors =========

    protected IncentiveWeekly() {}

    public IncentiveWeekly(Earner earner,
                           Integer cityId,
                           String week,
                           Integer targetJobs,
                           Integer completedJobs,
                           Double bonusEur) {
        this.earner = earner;
        this.cityId = cityId;
        this.week = week;
        this.targetJobs = targetJobs;
        this.completedJobs = completedJobs;
        this.bonusEur = bonusEur;
    }

    // ========= Getters/Setters =========

    public Long getId() { return id; }

    public Earner getEarner() { return earner; }
    public void setEarner(Earner earner) { this.earner = earner; }

    public Integer getCityId() { return cityId; }
    public void setCityId(Integer cityId) { this.cityId = cityId; }

    public String getWeek() { return week; }
    public void setWeek(String week) { this.week = week; }

    public Integer getTargetJobs() { return targetJobs; }
    public void setTargetJobs(Integer targetJobs) { this.targetJobs = targetJobs; }

    public Integer getCompletedJobs() { return completedJobs; }
    public void setCompletedJobs(Integer completedJobs) { this.completedJobs = completedJobs; }

    public Double getBonusEur() { return bonusEur; }
    public void setBonusEur(Double bonusEur) { this.bonusEur = bonusEur; }

    // ========= Utility Methods =========

    /** Progress ratio (0.0–1.0), useful for dashboards. */
    @Transient
    public double getProgressRatio() {
        if (targetJobs == null || targetJobs == 0) return 0.0;
        return Math.min(1.0, (double) completedJobs / targetJobs);
    }

    /** Whether the weekly incentive goal was achieved. */
    @Transient
    public boolean isCompleted() {
        return completedJobs != null && targetJobs != null && completedJobs >= targetJobs;
    }

    // ========= Equality =========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IncentiveWeekly)) return false;
        IncentiveWeekly other = (IncentiveWeekly) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
