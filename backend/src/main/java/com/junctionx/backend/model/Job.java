package com.junctionx.backend.model;

import com.junctionx.backend.model.enums.FulfillmentStatus;
import com.junctionx.backend.model.enums.Marketplace;
import jakarta.persistence.*;

import java.lang.Double;
import java.time.OffsetDateTime;

/**
 * Unified job (ride or delivery) completed by an Earner (driver/courier).
 * Minimal, driver-first fields; no duplicated/derivable data stored.
 */
@Entity
@Table(
        name = "jobs",
        indexes = {
                @Index(name = "idx_jobs_driver_time", columnList = "driver_id,start_time"),
                @Index(name = "idx_jobs_city_time",   columnList = "city_id,start_time"),
                @Index(name = "idx_jobs_pickup_hex",  columnList = "pickup_hex_id9"),
                @Index(name = "idx_jobs_drop_hex",    columnList = "drop_hex_id9")
        }
)
public class Job {

    @Id
    @Column(name = "job_id", nullable = false, updatable = false, length = 36)
    private String id;

    // ---- Relationships ----

    /** Driver/courier who fulfilled the job. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private Earner driver;

    /** Home/operating city for this job (FK). */
    @Column(name = "city_id", nullable = false)
    private Integer city;

    // ---- Identity on the demand side ----

    /**
     * Requester identifier:
     *  - ridesharing: rider_id
     *  - delivery: customer_id
     * Kept as String to avoid coupling to two different entities in one field.
     */
    @Column(name = "requester_id", length = 32, nullable = false)
    private String requesterId;

    // ---- Classification ----

    @Enumerated(EnumType.STRING)
    @Column(name = "marketplace", nullable = false, length = 16)
    private Marketplace marketplace; // RIDESHARING or DELIVERY

    /** UberX, UberGreen, UberPool, Eats-Delivery, ... */
    @Column(name = "product", length = 32, nullable = false)
    private String product;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_job_status", nullable = false, length = 16)
    private FulfillmentStatus fulfillmentJobStatus; // COMPLETED, CANCELED, ...

    // ---- Time window ----

    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    // ---- Pickup ----

    @Column(name = "pickup_lat")
    private Double pickupLat;

    @Column(name = "pickup_lon")
    private Double pickupLon;

    /** H3 index (resolution 9) for pickup area. */
    @Column(name = "pickup_hex_id9", length = 16)
    private String pickupHexId9;

    // ---- Drop ----

    @Column(name = "drop_lat")
    private Double dropLat;

    @Column(name = "drop_lon")
    private Double dropLon;

    /** H3 index (resolution 9) for drop area. */
    @Column(name = "drop_hex_id9", length = 16)
    private String dropHexId9;

    // ---- Effort ----
    
    @Column(name = "distance_km")
    private Double distanceKm;
    
    @Column(name = "duration_mins")
    private Integer durationMins;

    // ---- Money (driver-oriented) ----

    /**
     * Net earnings actually received by the driver for this job.
     * Store the final figure from source data/settlement.
     * (Tips are included here if your pipeline defines net that way.)
     */
    @Column(name = "net_earnings", nullable = false)
    private Double netEarnings;

    // ========= Constructors =========

    protected Job() {}

    public Job(String id,
               Earner driver,
               Integer city,
               String requesterId,
               Marketplace marketplace,
               String product,
               FulfillmentStatus fulfillmentJobStatus,
               OffsetDateTime startTime,
               OffsetDateTime endTime,
               Double pickupLat, Double pickupLon, String pickupHexId9,
               Double dropLat, Double dropLon, String dropHexId9,
               Double distanceKm, Integer durationMins,
               Double netEarnings) {

        this.id = id;
        this.driver = driver;
        this.city = city;
        this.requesterId = requesterId;
        this.marketplace = marketplace;
        this.product = product;
        this.fulfillmentJobStatus = fulfillmentJobStatus;
        this.startTime = startTime;
        this.endTime = endTime;
        this.pickupLat = pickupLat;
        this.pickupLon = pickupLon;
        this.pickupHexId9 = pickupHexId9;
        this.dropLat = dropLat;
        this.dropLon = dropLon;
        this.dropHexId9 = dropHexId9;
        this.distanceKm = distanceKm;
        this.durationMins = durationMins;
        this.netEarnings = netEarnings;
    }

    // ========= Getters/Setters =========

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Earner getDriver() { return driver; }
    public void setDriver(Earner driver) { this.driver = driver; }

    public Integer getCity() { return city; }
    public void setCity(Integer city) { this.city = city; }

    public String getRequesterId() { return requesterId; }
    public void setRequesterId(String requesterId) { this.requesterId = requesterId; }

    public Marketplace getMarketplace() { return marketplace; }
    public void setMarketplace(Marketplace marketplace) { this.marketplace = marketplace; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public FulfillmentStatus getFulfillmentJobStatus() { return fulfillmentJobStatus; }
    public void setFulfillmentJobStatus(FulfillmentStatus fulfillmentJobStatus) {
        this.fulfillmentJobStatus = fulfillmentJobStatus;
    }

    public OffsetDateTime getStartTime() { return startTime; }
    public void setStartTime(OffsetDateTime startTime) { this.startTime = startTime; }

    public OffsetDateTime getEndTime() { return endTime; }
    public void setEndTime(OffsetDateTime endTime) { this.endTime = endTime; }

    public Double getPickupLat() { return pickupLat; }
    public void setPickupLat(Double pickupLat) { this.pickupLat = pickupLat; }

    public Double getPickupLon() { return pickupLon; }
    public void setPickupLon(Double pickupLon) { this.pickupLon = pickupLon; }

    public String getPickupHexId9() { return pickupHexId9; }
    public void setPickupHexId9(String pickupHexId9) { this.pickupHexId9 = pickupHexId9; }

    public Double getDropLat() { return dropLat; }
    public void setDropLat(Double dropLat) { this.dropLat = dropLat; }

    public Double getDropLon() { return dropLon; }
    public void setDropLon(Double dropLon) { this.dropLon = dropLon; }

    public String getDropHexId9() { return dropHexId9; }
    public void setDropHexId9(String dropHexId9) { this.dropHexId9 = dropHexId9; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public Integer getDurationMins() { return durationMins; }
    public void setDurationMins(Integer durationMins) { this.durationMins = durationMins; }

    public Double getNetEarnings() { return netEarnings; }
    public void setNetEarnings(Double netEarnings) { this.netEarnings = netEarnings; }

    // ========= Equality by ID =========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Job)) return false;
        Job other = (Job) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return 31; }
}
