package com.junctionx.backend.service.impl;

import com.junctionx.backend.dto.DevDriverSummary;
import com.junctionx.backend.dto.DevEarningsCompare;
import com.junctionx.backend.dto.GeoJson;
import com.junctionx.backend.service.DevRegionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StubDevRegionService implements DevRegionService {

    @Override
    public List<DevDriverSummary> activeDrivers(String regionId, String date) {
        return List.of(
                new DevDriverSummary("E10111", 14, 132.4, 145.9),
                new DevDriverSummary("E10122", 11, 115.0, 126.2),
                new DevDriverSummary("E10133", 9,  98.7,  108.5)
        );
    }

    @Override
    public GeoJson.FeatureCollection actualTrips(String regionId, String date) {
        // two lines in the region to make sure FE can draw multiple
        List<GeoJson.Feature> features = new ArrayList<>();

        features.add(new GeoJson.Feature(
                Map.of(
                        "type", "LineString",
                        "coordinates", List.of(
                                List.of(4.3571, 52.0116),
                                List.of(4.3619, 52.0067)
                        )
                ),
                new LinkedHashMap<>(Map.of(
                        "tripId", "r-act-1",
                        "earnerId", "E10111",
                        "date", date,
                        "netEur", 11.3
                ))
        ));

        features.add(new GeoJson.Feature(
                Map.of(
                        "type", "LineString",
                        "coordinates", List.of(
                                List.of(4.3520, 52.0140),
                                List.of(4.3660, 52.0055)
                        )
                ),
                new LinkedHashMap<>(Map.of(
                        "tripId", "r-act-2",
                        "earnerId", "E10122",
                        "date", date,
                        "netEur", 9.8
                ))
        ));

        return new GeoJson.FeatureCollection(features);
    }

    @Override
    public GeoJson.FeatureCollection counterfactualTrips(String regionId, String date) {
        List<GeoJson.Feature> features = new ArrayList<>();

        features.add(new GeoJson.Feature(
                Map.of(
                        "type", "LineString",
                        "coordinates", List.of(
                                List.of(4.3550, 52.0130),
                                List.of(4.3680, 52.0045)
                        )
                ),
                new LinkedHashMap<>(Map.of(
                        "tripId", "r-cf-1",
                        "earnerId", "E10111",
                        "date", date,
                        "netEur", 12.7
                ))
        ));

        features.add(new GeoJson.Feature(
                Map.of(
                        "type", "LineString",
                        "coordinates", List.of(
                                List.of(4.3505, 52.0120),
                                List.of(4.3635, 52.0060)
                        )
                ),
                new LinkedHashMap<>(Map.of(
                        "tripId", "r-cf-2",
                        "earnerId", "E10122",
                        "date", date,
                        "netEur", 10.6
                ))
        ));

        return new GeoJson.FeatureCollection(features);
    }

    @Override
    public DevEarningsCompare earningsCompare(String regionId, String date) {
        // sum actual vs predicted across the sample
        double actual = 132.4 + 115.0 + 98.7;
        double predicted = 145.9 + 126.2 + 108.5;
        return new DevEarningsCompare(actual, predicted);
    }
}
