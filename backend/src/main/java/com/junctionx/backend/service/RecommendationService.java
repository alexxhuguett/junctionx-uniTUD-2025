package com.junctionx.backend.service;

import com.junctionx.backend.dto.RecommendationDTO;
import java.util.List;

public interface RecommendationService {
    List<RecommendationDTO> recommendations(String earnerId, String date, String now);
}
