package com.junctionx.backend.dto;

// Aided by LLM
public class RecommendationDTO {
    private String type;          // "earnings" | "wellness" | "incentive"
    private String message;
    private String hex;           // optional (for map centering)
    private int validForMins;

    public RecommendationDTO() {}
    public RecommendationDTO(String type, String message, String hex, int validForMins) {
        this.type = type; this.message = message; this.hex = hex; this.validForMins = validForMins;
    }
    public String getType() { return type; }
    public String getMessage() { return message; }
    public String getHex() { return hex; }
    public int getValidForMins() { return validForMins; }
    public void setType(String type) { this.type = type; }
    public void setMessage(String message) { this.message = message; }
    public void setHex(String hex) { this.hex = hex; }
    public void setValidForMins(int validForMins) { this.validForMins = validForMins; }
}
