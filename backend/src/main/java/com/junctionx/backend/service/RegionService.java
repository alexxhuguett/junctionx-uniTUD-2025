package com.junctionx.backend.service;

import com.junctionx.backend.dto.GeoJson.FeatureCollection;

// Aided by LLM
public interface RegionService {
    FeatureCollection actualTrips(int cityId, String date, Integer page, Integer size, String earnerIdsCsv);
    FeatureCollection counterfactualTrips(int cityId, String date, Integer page, Integer size, String earnerIdsCsv);
    FeatureCollection heatmap(int cityId, String date);
}
