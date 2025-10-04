package com.junctionx.backend.service;

import com.junctionx.backend.dto.GeoJson;
import java.time.LocalDate;

public interface DriverTripsService {
    GeoJson.FeatureCollection getTrips(String driverId, LocalDate date);
    GeoJson.FeatureCollection getCounterfactualTrips(String driverId, LocalDate date);
}
