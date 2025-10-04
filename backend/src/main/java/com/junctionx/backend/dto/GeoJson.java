package com.junctionx.backend.dto;

import java.util.List;
import java.util.Map;

// Aided by LLM
public class GeoJson {

    // One "Feature" (geometry + properties)
    public static class Feature {
        private String type = "Feature";
        private Map<String, Object> geometry;      // e.g. { "type": "LineString", "coordinates": [[lon,lat], [lon,lat]] }
        private Map<String, Object> properties;    // arbitrary props (tripId, earnings, etc.)

        public Feature() {}
        public Feature(Map<String, Object> geometry, Map<String, Object> properties) {
            this.geometry = geometry;
            this.properties = properties;
        }

        public String getType() { return type; }
        public Map<String, Object> getGeometry() { return geometry; }
        public Map<String, Object> getProperties() { return properties; }

        public void setGeometry(Map<String, Object> geometry) { this.geometry = geometry; }
        public void setProperties(Map<String, Object> properties) { this.properties = properties; }
    }

    // A collection of features
    public static class FeatureCollection {
        private String type = "FeatureCollection";
        private List<Feature> features;

        public FeatureCollection() {}
        public FeatureCollection(List<Feature> features) { this.features = features; }

        public String getType() { return type; }
        public List<Feature> getFeatures() { return features; }
        public void setFeatures(List<Feature> features) { this.features = features; }
    }
}
