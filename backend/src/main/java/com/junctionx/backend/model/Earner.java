package com.junctionx.backend.model;

import com.junctionx.backend.model.enums.EarnerType;
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
    private String earnerId;


    @Enumerated(EnumType.STRING)
    @Column(name = "earner_type", nullable = false, length = 16)
    private EarnerType earnerType;


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
    @Column(name = "home_city_id", nullable = false)
    private Integer homeCityId;

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
                  Double rating, Integer homeCityId) {
        this.earnerId = id;
        this.vehicleType = vehicleType;
        this.fuelType = fuelType;
        this.rating = rating;
        this.homeCityId = homeCityId;
    }

    // ========= Getters/Setters =========


    public String getEarnerId() {
        return earnerId;
    }

    public void setEarnerId(String earnerId) {
        this.earnerId = earnerId;
    }

    public EarnerType getEarnerType() {
        return earnerType;
    }


    public void setEarnerType(EarnerType earnerType) {
        this.earnerType = earnerType;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getHomeCityId() {
        return homeCityId;
    }

    public void setHomeCityId(Integer homeInteger) {
        this.homeCityId = homeInteger;
    }

    public Set<Job> getJobs() {
        return jobs;
    }

    public void setJobs(Set<Job> jobs) {
        this.jobs = jobs;
    }

    public Set<IncentiveWeekly> getIncentives() {
        return incentives;
    }

    public void setIncentives(Set<IncentiveWeekly> incentives) {
        this.incentives = incentives;
    }

    // ========= Equality by ID =========
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Earner)) return false;
        Earner other = (Earner) o;
        return earnerId != null && earnerId.equals(other.earnerId);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
