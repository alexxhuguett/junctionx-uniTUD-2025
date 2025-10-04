import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "job",
        indexes = {
                @Index(name = "idx_job_city", columnList = "city_id"),
                @Index(name = "idx_job_pickup_hex", columnList = "pickup_hex_id9"),
                @Index(name = "idx_job_driver_start", columnList = "driver_id,start_time"),
                @Index(name = "idx_job_drop_hex", columnList = "drop_hex_id9"),
                @Index(name = "idx_job_completed", columnList = "is_completed")
        }
)
public class Job {

    @Id
    @Column(name = "job_uuid", length = 64, nullable = false)
    private String jobUuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", referencedColumnName = "earner_id", nullable = false)
    private Earner driver;

    @Column(name = "city_id")
    private Integer cityId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "product", length = 32)
    private ProductType product;

    @Column(name = "pickup_hex_id9", length = 32)
    private String pickupHexId9;

    @Column(name = "drop_hex_id9", length = 32)
    private String dropHexId9;

    @Column(name = "distance_km", precision = 8, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "duration_mins")
    private Integer durationMins;

    @Column(name = "net_earnings", precision = 12, scale = 2)
    private BigDecimal netEarnings;

    @Column(name = "tips", precision = 12, scale = 2)
    private BigDecimal tips;

    @Column(name = "fare_amount", precision = 12, scale = 2)
    private BigDecimal fareAmount;

    @Column(name = "uber_fee", precision = 12, scale = 2)
    private BigDecimal uberFee;

    @Column(name = "surge_multiplier", precision = 5, scale = 2)
    private BigDecimal surgeMultiplier;

    @Column(name = "payment_type", length = 32)
    private String paymentType;

    @Column(name = "is_completed", nullable = false)
    private Boolean completed;

    public Job() {
    }
}