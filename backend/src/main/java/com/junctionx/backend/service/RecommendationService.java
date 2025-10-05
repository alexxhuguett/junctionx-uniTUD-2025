package com.junctionx.backend.service;

import com.junctionx.backend.dto.RecommendationDTO;

import java.time.Duration;
import java.util.List;

public interface RecommendationService {
    List<RecommendationDTO> recommendations(String earnerId, String date, String now);
    default void nudgeBreak(String earnerId, Duration drivingTime) { }
    default void nudgeBonusAchieved(String earnerId, double bonusEur, String week) { }
    default void nudgeBonusClose(String earnerId, int remaining, String week, double bonusEur) { }
}
