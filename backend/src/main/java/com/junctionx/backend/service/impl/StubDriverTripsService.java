package com.junctionx.backend.service.impl;

import com.junctionx.backend.dto.GeoJson;
import com.junctionx.backend.service.DriverTripsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StubDriverTripsService implements DriverTripsService {

    @Override
    public GeoJson.FeatureCollection getTrips(String driverId, LocalDate date) {
        // Geometry (LineString)
        Map<String, Object> geometry = new LinkedHashMap<>();
        geometry.put("type", "LineString");
        List<double[]> coords = new ArrayList<>();
        coords.add(new double[]{4.3571, 52.0116}); // [lon, lat]
        coords.add(new double[]{4.3619, 52.0067});
        geometry.put("coordinates", coords);

        // Properties
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("tripId", "demo-1");
        props.put("start", date.atTime(9, 0).toString());
        props.put("end",   date.atTime(9, 20).toString());
        props.put("pickupHex", "89b5443252677be");
        props.put("dropHex",   "896326c3e14b5c2");
        props.put("distanceKm", 6.2);
        props.put("durationMins", 20);
        props.put("surge", 1.15);
        props.put("netEur", 11.30);
        props.put("tipsEur", 0.0);
        props.put("earnerId", driverId);

        GeoJson.Feature feature = new GeoJson.Feature(geometry, props);
        List<GeoJson.Feature> features = new ArrayList<>();
        features.add(feature);
        return new GeoJson.FeatureCollection(features);
    }

    @Override
    public GeoJson.FeatureCollection getCounterfactualTrips(String driverId, LocalDate date) {
        Map<String, Object> geometry = new LinkedHashMap<>();
        geometry.put("type", "LineString");
        List<double[]> coords = new ArrayList<>();
        coords.add(new double[]{4.3585, 52.0125});
        coords.add(new double[]{4.3650, 52.0079});
        geometry.put("coordinates", coords);

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("tripId", "cf-1");
        props.put("start", date.atTime(9, 5).toString());
        props.put("end",   date.atTime(9, 22).toString());
        props.put("pickupHex", "89b5443252677be");
        props.put("dropHex",   "896326c3e14b5c2");
        props.put("distanceKm", 5.8);
        props.put("durationMins", 17);
        props.put("surge", 1.25);
        props.put("netEur", 12.40);
        props.put("tipsEur", 0.5);
        props.put("earnerId", driverId);

        GeoJson.Feature feature = new GeoJson.Feature(geometry, props);
        List<GeoJson.Feature> features = new ArrayList<>();
        features.add(feature);
        return new GeoJson.FeatureCollection(features);
    }
}
