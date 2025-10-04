package com.junctionx.backend.model;

import jakarta.persistence.*;

/**
 * Predicted earnings heatmap per city & H3 hex (resolution 9).
 * Schema: city_id, hexagon_id9, predicted_eph, predicted_std
 */
@Entity
@Table(
        name = "heatmap",
        indexes = {
                @Index(name = "idx_heatmap_city_hex", columnList = "city_id,hexagon_id9"),
                @Index(name = "idx_heatmap_city",     columnList = "city_id")
        }
)
public class HeatMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "heatmap_id")
    private Long id;

    /** City identifier (no City table). */
    @Column(name = "city_id", nullable = false)
    private Integer cityId;

    /** H3 index (res 9) identifying the zone. */
    @Column(name = "hexagon_id9", length = 16, nullable = false)
    private String hexagonId9;

    /** Predicted earnings per hour for drivers in this hex (e.g., 22.18 = €22.18/hr). */
    @Column(name = "predicted_eph", nullable = false)
    private Double predictedEph;

    /** Standard deviation (uncertainty) of the prediction. Lower = more confident. */
    @Column(name = "predicted_std", nullable = false)
    private Double predictedStd;

    // ========= Constructors =========

    protected HeatMap() {}

    public HeatMap(Integer cityId, String hexagonId9, Double predictedEph, Double predictedStd) {
        this.cityId = cityId;
        this.hexagonId9 = hexagonId9;
        this.predictedEph = predictedEph;
        this.predictedStd = predictedStd;
    }

    // ========= Getters/Setters =========

    public Long getId() { return id; }

    public Integer getCityId() { return cityId; }
    public void setCityId(Integer cityId) { this.cityId = cityId; }

    public String getHexagonId9() { return hexagonId9; }
    public void setHexagonId9(String hexagonId9) { this.hexagonId9 = hexagonId9; }

    public Double getPredictedEph() { return predictedEph; }
    public void setPredictedEph(Double predictedEph) { this.predictedEph = predictedEph; }

    public Double getPredictedStd() { return predictedStd; }
    public void setPredictedStd(Double predictedStd) { this.predictedStd = predictedStd; }

    // ========= Utility =========

    /** Simple confidence heuristic (0..1): lower std => higher confidence. */
    @Transient
    public double getConfidenceScore() {
        if (predictedStd == null || predictedStd <= 0) return 1.0;
        // tune as you like; 10 is a reasonable cap for your mock data scale
        return Math.max(0.0, Math.min(1.0, 1.0 - (predictedStd / 10.0)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HeatMap)) return false;
        HeatMap other = (HeatMap) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return 31; }
}

