package com.junctionx.backend.service.impl;

import com.junctionx.backend.dto.RecommendationDTO;
import com.junctionx.backend.service.RecommendationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StubRecommendationService implements RecommendationService {

    @Override
    public List<RecommendationDTO> recommendations(String earnerId, String date, String now) {
        // Tiny deterministic sample set for the FE cards
        return List.of(
                new RecommendationDTO(
                        "rec-1", "start_time",
                        "Start your shift at 08:45 to catch the morning demand bump.",
                        date + "T08:30:00", date + "T09:30:00",
                        null, 6.50, 1
                ),
                new RecommendationDTO(
                        "rec-2", "move_to_hex",
                        "Move towards hex 89b5443252677be (north of city center).",
                        date + "T10:00:00", date + "T12:00:00",
                        "89b5443252677be", 4.10, 2
                ),
                new RecommendationDTO(
                        "rec-3", "break",
                        "Take a short break 14:30–14:50, demand dips then.",
                        date + "T14:30:00", date + "T14:50:00",
                        null, 1.10, 3
                )
        );
    }
}
