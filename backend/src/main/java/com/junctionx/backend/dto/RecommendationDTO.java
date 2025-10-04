package com.junctionx.backend.dto;

public class RecommendationDTO {
    private String id;
    private String type;              // e.g., "start_time", "move_to_hex"
    private String message;           // human-readable text for the card
    private String effectiveFrom;     // ISO-8601 time
    private String effectiveTo;       // ISO-8601 time
    private String targetHex;         // optional H3
    private double expectedUpliftEur; // expected daily uplift
    private int priority;             // 1 = highest

    public RecommendationDTO() {}

    public RecommendationDTO(String id, String type, String message,
                             String effectiveFrom, String effectiveTo,
                             String targetHex, double expectedUpliftEur, int priority) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.targetHex = targetHex;
        this.expectedUpliftEur = expectedUpliftEur;
        this.priority = priority;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getMessage() { return message; }
    public String getEffectiveFrom() { return effectiveFrom; }
    public String getEffectiveTo() { return effectiveTo; }
    public String getTargetHex() { return targetHex; }
    public double getExpectedUpliftEur() { return expectedUpliftEur; }
    public int getPriority() { return priority; }

    public void setId(String id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setMessage(String message) { this.message = message; }
    public void setEffectiveFrom(String effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    public void setEffectiveTo(String effectiveTo) { this.effectiveTo = effectiveTo; }
    public void setTargetHex(String targetHex) { this.targetHex = targetHex; }
    public void setExpectedUpliftEur(double expectedUpliftEur) { this.expectedUpliftEur = expectedUpliftEur; }
    public void setPriority(int priority) { this.priority = priority; }
}
