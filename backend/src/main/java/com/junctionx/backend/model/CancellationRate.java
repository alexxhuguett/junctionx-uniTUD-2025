package com.junctionx.backend.model;

import jakarta.persistence.*;

/**
 * Cancellation rate by city and hexagonal zone (H3 index at resolution 9).
 * Schema: city_id, hexagon_id9, cancellation_rate
 */
@Entity
@Table(
        name = "cancellation_rates",
        indexes = {
                @Index(name = "idx_cancel_city_hex", columnList = "city_id,hexagon_id9")
        }
)
public class CancellationRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cancellation_id")
    private Long id;

    /** City identifier (integer, not a separate entity). */
    @Column(name = "city_id", nullable = false)
    private Integer cityId;

    /** H3 geospatial index (resolution 9). Represents a small zone in the city. */
    @Column(name = "hexagon_id9", length = 16, nullable = false)
    private String hexagonId9;

    /** Percentage of jobs cancelled in this zone (e.g., 5.0 = 5%). */
    @Column(name = "cancellation_rate", nullable = false)
    private Double cancellationRate;

    // ========= Constructors =========

    protected CancellationRate() {}

    public CancellationRate(Integer cityId, String hexagonId9, Double cancellationRate) {
        this.cityId = cityId;
        this.hexagonId9 = hexagonId9;
        this.cancellationRate = cancellationRate;
    }

    // ========= Getters/Setters =========

    public Long getId() { return id; }

    public Integer getCityId() { return cityId; }
    public void setCityId(Integer cityId) { this.cityId = cityId; }

    public String getHexagonId9() { return hexagonId9; }
    public void setHexagonId9(String hexagonId9) { this.hexagonId9 = hexagonId9; }

    public Double getCancellationRate() { return cancellationRate; }
    public void setCancellationRate(Double cancellationRate) { this.cancellationRate = cancellationRate; }

    // ========= Utility =========

    /** Returns true if this zone has a high cancellation rate (>10%). */
    @Transient
    public boolean isHighCancellationZone() {
        return cancellationRate != null && cancellationRate > 10.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CancellationRate)) return false;
        CancellationRate other = (CancellationRate) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return 31; }
}
