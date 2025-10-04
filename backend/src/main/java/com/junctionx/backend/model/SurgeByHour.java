@Entity
@Table(
        name = "surge_by_hour",
        indexes = @Index(name = "idx_surge_city_hour", columnList = "city_id,hour"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"city_id","hour"})
)
public class SurgeByHour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "city_id", nullable = false)
    private Integer cityId;

    @Column(name = "hour", nullable = false)
    private Short hour;

    @Column(name = "surge_multiplier", precision = 5, scale = 2)
    private BigDecimal surgeMultiplier;

    public SurgeByHour() {}
}