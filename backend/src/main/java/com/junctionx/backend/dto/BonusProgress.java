package com.junctionx.backend.dto;

public record BonusProgress(
        String week, int completed, int target, int remaining,
        boolean achieved, double bonusEur
) {}
