package com.junctionx.backend.model;

import com.junctionx.backend.model.enums.FuelType;
import com.junctionx.backend.model.enums.VehicleType;
import jakarta.persistence.*;

import java.lang.Double;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Supply-side participant (driver/courier).
 * Matches rows in the `earners` sheet: id, vehicle_type, fuel_type, rating, home_Integer_id.
 */
@Entity
@Table(name = "earners")
public class Earner {

    @Id
    @Column(name = "earner_id", nullable = false, updatable = false, length = 16)
    private String earner_id;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 16)
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false, length = 16)
    private FuelType fuelType;

    /**
     * Driver/courier rating
     */
    @Column(name = "rating", nullable = false)
    private Double rating;

    /**
     * Home Integer reference (FK to Integer.id). If you don’t have a Integer entity yet,
     * temporarily switch this to an Integer `homeIntegerId`.
     */
    @Column(name = "home_Integer_id", nullable = false)
    private Integer homeInteger;

    /**
     * All jobs this earner fulfilled (rides or deliveries).
     * Mapped by `driver` field in Job entity.
     */
    @OneToMany(mappedBy = "driver", orphanRemoval = false)
    private Set<Job> jobs = new LinkedHashSet<>();

    /**
     * Weekly incentives (quests) earned by this earner.
     * Mapped by `earner` field in IncentiveWeekly entity.
     */
    @OneToMany(mappedBy = "earner", orphanRemoval = false)
    private Set<IncentiveWeekly> incentives = new LinkedHashSet<>();

    // ========= Constructors =========
    protected Earner() { }

    public Earner(String id, VehicleType vehicleType, FuelType fuelType,
                  Double rating, Integer homeInteger) {
        this.earner_id = id;
        this.vehicleType = vehicleType;
        this.fuelType = fuelType;
        this.rating = rating;
        this.homeInteger = homeInteger;
    }

    // ========= Getters/Setters =========
    public String getId() { return earner_id; }
    public void setId(String id) { this.earner_id = id; }

    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }

    public FuelType getFuelType() { return fuelType; }
    public void setFuelType(FuelType fuelType) { this.fuelType = fuelType; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getHomeInteger() { return homeInteger; }
    public void setHomeInteger(Integer homeInteger) { this.homeInteger = homeInteger; }

    public Set<Job> getJobs() { return jobs; }
    public Set<IncentiveWeekly> getIncentives() { return incentives; }

    // ========= Equality by ID =========
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Earner)) return false;
        Earner other = (Earner) o;
        return earner_id != null && earner_id.equals(other.earner_id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
