package com.junctionx.backend.service.impl;

import com.junctionx.backend.dto.GeoJson.Feature;
import com.junctionx.backend.dto.GeoJson.FeatureCollection;
import com.junctionx.backend.service.RegionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

// Aided by LLM
@Service
public class StubRegionService implements RegionService {
    @Override
    public FeatureCollection actualTrips(int cityId, String date, Integer page, Integer size, String earnerIdsCsv) {
        var f = new Feature(
                Map.of("type","LineString","coordinates", List.of(List.of(4.35,52.01), List.of(4.37,52.00))),
                Map.of("tripId","r-1","earnerId","E10111","start",date+"T10:00:00","netEur",13.4)
        );
        return new FeatureCollection(List.of(f));
    }

    @Override
    public FeatureCollection counterfactualTrips(int cityId, String date, Integer page, Integer size, String earnerIdsCsv) {
        var f = new Feature(
                Map.of("type","LineString","coordinates", List.of(List.of(4.34,52.02), List.of(4.38,51.99))),
                Map.of("tripId","r-cf-1","earnerId","E10111","start",date+"T10:05:00","netEur",15.2)
        );
        return new FeatureCollection(List.of(f));
    }

    @Override
    public FeatureCollection heatmap(int cityId, String date) {
        var point = new Feature(
                Map.of("type","Point","coordinates", List.of(4.88997, 52.36991)),
                Map.of("hex9","89fb0333e75a7e5","predictedEph",22.18,"std",4.28,"inclusion",true)
        );
        return new FeatureCollection(List.of(point));
    }
}
