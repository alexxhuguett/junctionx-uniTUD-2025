package com.junctionx.backend.service;

import com.junctionx.backend.dto.QualityDTO;

// Aided by LLM
public interface QualityService {
    QualityDTO quality(String earnerId, String date);
}
