package com.junctionx.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "heatmap")
public class HeatMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // new surrogate PK
    private Long id;

    @Column(name = "map_id", nullable = false, length = 16) // previously the PK
    private String mapId;

    @Column(name = "city_id")
    private Integer cityId;

    /** H3 index (res 9) identifying the zone. */
    @Column(name = "hexagon_id9", length = 16, nullable = false)
    private String hexagonId9;

    @Column(name = "predicted_eph")
    private Double predictedEph;

    @Column(name = "predicted_std")
    private Double predictedStd;

    protected HeatMap() {}

    // Getters / setters

    public Long getId() { return id; }

    public String getMapId() { return mapId; }
    public void setMapId(String mapId) { this.mapId = mapId; }

    public Integer getCityId() { return cityId; }
    public void setCityId(Integer cityId) { this.cityId = cityId; }

    public String getHexagonId9() { return hexagonId9; }
    public void setHexagonId9(String hexagonId9) { this.hexagonId9 = hexagonId9; }

    public Double getPredictedEph() { return predictedEph; }
    public void setPredictedEph(Double predictedEph) { this.predictedEph = predictedEph; }

    public Double getPredictedStd() { return predictedStd; }
    public void setPredictedStd(Double predictedStd) { this.predictedStd = predictedStd; }

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
