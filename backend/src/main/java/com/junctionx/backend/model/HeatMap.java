@Entity @Table(name="heatmap",
  indexes=@Index(name="idx_heatmap_city_map_hex", columnList="msg_city_id,msg_map_id,hexagon_id9"))
public class Heatmap {

  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  @Column(name="msg_city_id")
  private Integer msgCityId;

  @Column(name="msg_map_id")
  private String msgMapId;

  @Column(name="hexagon_id9")
  private String hexagonId9;

  @Column(name="predicted_eph", precision=10, scale=2)
  private BigDecimal predictedEph;

  @Column(name="predicted_std", precision=10, scale=2)
  private BigDecimal predictedStd;

  @Column(name="in_final_heatmap")
  private Boolean inFinalHeatmap;
}