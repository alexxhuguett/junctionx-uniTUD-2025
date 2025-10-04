@Entity
@Table(
        name = "incentives_weekly",
        indexes = @Index(name = "idx_incentive_earner_week", columnList = "earner_id,week_start"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"earner_id","week_start","program"})
)
public class IncentivesWeekly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK to earner_id in your earner table
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "earner_id", referencedColumnName = "earner_id", nullable = false)
    private Earner earner;

    // FIX: store week as Monday LocalDate instead of a String
    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Column(name = "program", nullable = false)
    private String program;

    @Column(name = "target_jobs")
    private Integer targetJobs;

    @Column(name = "completed_jobs")
    private Integer completedJobs;

    @Column(name = "achieved")
    private Boolean achieved;

    @Column(name = "bonus_eur", precision = 12, scale = 2)
    private BigDecimal bonusEur;

    public IncentivesWeekly() {}
}