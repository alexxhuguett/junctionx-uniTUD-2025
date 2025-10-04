package com.junctionx.backend.service;

import com.junctionx.backend.dto.DevDriverSummary;
import com.junctionx.backend.dto.DevEarningsCompare;
import com.junctionx.backend.dto.GeoJson;

import java.util.List;

public interface DevRegionService {
    List<DevDriverSummary> activeDrivers(String regionId, String date);
    GeoJson.FeatureCollection actualTrips(String regionId, String date);
    GeoJson.FeatureCollection counterfactualTrips(String regionId, String date);
    DevEarningsCompare earningsCompare(String regionId, String date);
}
