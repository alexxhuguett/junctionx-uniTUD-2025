package com.junctionx.backend.service.impl;

import com.junctionx.backend.dto.QualityDTO;
import com.junctionx.backend.dto.QualityDTO.Component;
import com.junctionx.backend.service.QualityService;
import org.springframework.stereotype.Service;

import java.util.List;

// Aided by LLM
@Service
public class StubQualityService implements QualityService {
    @Override
    public QualityDTO quality(String earnerId, String date) {
        return new QualityDTO(
                earnerId, date, 84,
                List.of(
                        new Component("rating", 4.79, 0.5),
                        new Component("completionRate", 0.96, 0.3),
                        new Component("questBoost", 10.0, 0.2)
                ),
                "High rating and strong completion; moderate quest progress."
        );
    }
}
