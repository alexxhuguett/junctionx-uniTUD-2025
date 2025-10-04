package com.junctionx.backend.service;

import com.junctionx.backend.dto.GeoJson;

import java.time.LocalDate;

public interface DriverTripsService {

    // Per-driver, specific date (used by the “driverId + LocalDate” endpoints)
    GeoJson.FeatureCollection getTrips(String driverId, LocalDate date);
    GeoJson.FeatureCollection getCounterfactualTrips(String driverId, LocalDate date);

    // Earner endpoints used by DriverController (date as String)
    GeoJson.FeatureCollection actualTrips(String earnerId, String date, Integer page, Integer size);
    GeoJson.FeatureCollection counterfactualTrips(String earnerId, String date, Integer page, Integer size);
}
