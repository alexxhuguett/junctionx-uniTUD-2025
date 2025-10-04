package com.junctionx.backend.service.impl;

import com.junctionx.backend.dto.QualityDTO;
import com.junctionx.backend.service.QualityService;
import org.springframework.stereotype.Service;

@Service
public class StubQualityService implements QualityService {
    @Override
    public QualityDTO quality(String earnerId, String date) {
        // Simple static KPIs that look reasonable for demo
        return new QualityDTO(
                0.92,   // acceptanceRate (92%)
                96.0,   // onTimePct (96%)
                0.03,   // cancelRate (3%)
                6.4     // avgPickupLatencyMins
        );
    }
}
