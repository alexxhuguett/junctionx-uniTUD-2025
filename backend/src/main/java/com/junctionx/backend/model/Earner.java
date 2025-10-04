package com.junctionx.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "earners")
public class Earner {

    @Id
    @Column(name = "earner_id", length = 64, nullable = false)
    private String earnerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "earner_type", nullable = false, length = 16)
    private EarnerType earnerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 16)
    private VehicleType vehicleType;

    @Column(name = "is_ev")
    private Boolean isEv;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "home_city_id")
    private Integer homeCityId;

    @OneToMany(mappedBy = "driver", fetch = FetchType.LAZY)
    private Set<Job> jobs = new HashSet<>();
}
