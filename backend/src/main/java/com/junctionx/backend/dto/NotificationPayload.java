package com.junctionx.backend.dto;

public record NotificationPayload(
        String type,          // "BREAK", "BONUS_ACHIEVED", "BONUS_CLOSE"
        String message,
        String createdAt      // ISO string for convenience in UI
) {}

