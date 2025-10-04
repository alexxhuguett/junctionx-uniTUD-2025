package com.junctionx.backend.service;

// Aided by LLM
import com.junctionx.backend.dto.GeoJson.FeatureCollection;

public interface DriverTripsService {
    FeatureCollection actualTrips(String earnerId, String date, Integer page, Integer size);
    FeatureCollection counterfactualTrips(String earnerId, String date, Integer page, Integer size);
}
