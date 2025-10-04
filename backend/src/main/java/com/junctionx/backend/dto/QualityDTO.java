package com.junctionx.backend.dto;

// Aided by LLM
import java.util.List;

public class QualityDTO {
    public static class Component {
        private String name;
        private double value;
        private double weight;

        public Component() {}
        public Component(String name, double value, double weight) { this.name = name; this.value = value; this.weight = weight; }
        public String getName() { return name; }
        public double getValue() { return value; }
        public double getWeight() { return weight; }
        public void setName(String name) { this.name = name; }
        public void setValue(double value) { this.value = value; }
        public void setWeight(double weight) { this.weight = weight; }
    }

    private String earnerId;
    private String date;
    private int score;
    private List<Component> components;
    private String rationale;

    public QualityDTO() {}
    public QualityDTO(String earnerId, String date, int score, List<Component> components, String rationale) {
        this.earnerId = earnerId; this.date = date; this.score = score; this.components = components; this.rationale = rationale;
    }
    public String getEarnerId() { return earnerId; }
    public String getDate() { return date; }
    public int getScore() { return score; }
    public List<Component> getComponents() { return components; }
    public String getRationale() { return rationale; }
    public void setEarnerId(String earnerId) { this.earnerId = earnerId; }
    public void setDate(String date) { this.date = date; }
    public void setScore(int score) { this.score = score; }
    public void setComponents(List<Component> components) { this.components = components; }
    public void setRationale(String rationale) { this.rationale = rationale; }
}
