package com.junctionx.backend.service.impl;

import com.junctionx.backend.dto.EarningsDTO.DayCompare;
import com.junctionx.backend.dto.EarningsDTO.DayTotals;
import com.junctionx.backend.dto.EarningsDTO.RollingAvg;
import com.junctionx.backend.dto.EarningsDTO.RegionCompare;
import com.junctionx.backend.service.EarningsService;
import org.springframework.stereotype.Service;

// Aided by LLM
@Service
public class StubEarningsService implements EarningsService {

    @Override
    public DayCompare dayCompare(String earnerId, String date) {
        var actual = new DayTotals(6, 92.3, 8.0, 340);
        var predicted = new DayTotals(7, 104.6, 8.0, 335);
        return new DayCompare(date, earnerId, actual, predicted, 13.3);
    }

    @Override
    public RollingAvg rollingAvg(String earnerId, Integer windowDays) {
        int wd = (windowDays == null ? 28 : windowDays);
        return new RollingAvg(earnerId, wd, 86.2, 93.7, 8.7);
    }

    @Override
    public RegionCompare regionCompare(int cityId, String date) {
        return new RegionCompare(date, cityId, 12540.0, 13880.0, 10.7, 312);
    }
}
