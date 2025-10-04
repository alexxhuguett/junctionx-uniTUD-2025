package com.junctionx.backend.model;

import jakarta.persistence.*;

/**
 * Surge multiplier by city and hour.
 * Schema: city_id, hour, surge_multiplier
 */
@Entity
@Table(
        name = "surge_by_hour",
        indexes = {
                @Index(name = "idx_surge_city_hour", columnList = "city_id,hour")
        }
)
public class SurgeByHour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "surge_id")
    private Long id;

    /** City identifier (no City entity, plain int). */
    @Column(name = "city_id", nullable = false)
    private Integer cityId;

    /** Hour of day (0–23). */
    @Column(name = "hour", nullable = false)
    private Integer hour;

    /** Surge multiplier (e.g., 1.0 = normal, 1.5 = 50% surge). */
    @Column(name = "surge_multiplier", nullable = false)
    private Double surgeMultiplier;

    // ========= Constructors =========

    protected SurgeByHour() {}

    public SurgeByHour(Integer cityId, Integer hour, Double surgeMultiplier) {
        this.cityId = cityId;
        this.hour = hour;
        this.surgeMultiplier = surgeMultiplier;
    }

    // ========= Getters/Setters =========

    public Long getId() { return id; }

    public Integer getCityId() { return cityId; }
    public void setCityId(Integer cityId) { this.cityId = cityId; }

    public Integer getHour() { return hour; }
    public void setHour(Integer hour) { this.hour = hour; }

    public Double getSurgeMultiplier() { return surgeMultiplier; }
    public void setSurgeMultiplier(Double surgeMultiplier) { this.surgeMultiplier = surgeMultiplier; }

    // ========= Utility =========

    /** Returns true if the hour is considered surge time (>1.0). */
    @Transient
    public boolean isSurging() {
        return surgeMultiplier != null && surgeMultiplier > 1.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SurgeByHour)) return false;
        SurgeByHour other = (SurgeByHour) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return 31; }
}
