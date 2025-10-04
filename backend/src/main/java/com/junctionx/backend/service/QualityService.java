package com.junctionx.backend.service;

import com.junctionx.backend.dto.QualityDTO;

public interface QualityService {
    QualityDTO quality(String earnerId, String date);
}
