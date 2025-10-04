package com.junctionx.backend.service.impl;

import com.junctionx.backend.dto.GeoJson.Feature;
import com.junctionx.backend.dto.GeoJson.FeatureCollection;
import com.junctionx.backend.service.DriverTripsService;
import org.springframework.stereotype.Service;

import java.util.*;

// Aided by LLM
@Service
public class StubDriverTripsService implements DriverTripsService {

    @Override
    public FeatureCollection actualTrips(String earnerId, String date, Integer page, Integer size) {
        Map<String, Object> geom = Map.of(
                "type", "LineString",
                "coordinates", List.of(
                        List.of(4.3571, 52.0116),
                        List.of(4.3619, 52.0067)
                )
        );

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("tripId", "demo-1");
        props.put("start", date + "T09:00:00");
        props.put("end", date + "T09:20:00");
        props.put("pickupHex", "89b5443252677be");
        props.put("dropHex", "896326c3e14b5c2");
        props.put("distanceKm", 6.2);
        props.put("durationMins", 20);
        props.put("surge", 1.15);
        props.put("netEur", 11.3);
        props.put("tipsEur", 0.0);
        props.put("earnerId", earnerId);

        return new FeatureCollection(List.of(new Feature(geom, props)));
    }

    @Override
    public FeatureCollection counterfactualTrips(String earnerId, String date, Integer page, Integer size) {
        Map<String, Object> geom = Map.of(
                "type", "LineString",
                "coordinates", List.of(
                        List.of(4.3550, 52.0130),
                        List.of(4.3680, 52.0045)
                )
        );

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("tripId", "cf-1");
        props.put("start", date + "T09:05:00");
        props.put("end", date + "T09:23:00");
        props.put("pickupHex", "89b5443252677be");
        props.put("dropHex", "896326c3e14b5c2");
        props.put("distanceKm", 5.8);
        props.put("durationMins", 18);
        props.put("surge", 1.25);
        props.put("netEur", 12.9);
        props.put("tipsEur", 0.0);
        props.put("earnerId", earnerId);

        return new FeatureCollection(List.of(new Feature(geom, props)));
    }
}
