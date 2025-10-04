package com.junctionx.backend.service.impl;

import com.junctionx.backend.dto.RecommendationDTO;
import com.junctionx.backend.service.RecommendationService;
import org.springframework.stereotype.Service;

import java.util.List;

// Aided by LLM
@Service
public class StubRecommendationService implements RecommendationService {
    @Override
    public List<RecommendationDTO> recommendations(String earnerId, String date, String now) {
        return List.of(
                new RecommendationDTO("earnings", "Next 15 min: high demand near Delft Station (+25% surge).", "89b5443252677be", 20),
                new RecommendationDTO("wellness", "You’ve been online for 2h 45m — take a 10-min break.", null, 15),
                new RecommendationDTO("incentive", "Stay until 17:30 to hit the lunch bonus (+€15).", null, 90)
        );
    }
}
