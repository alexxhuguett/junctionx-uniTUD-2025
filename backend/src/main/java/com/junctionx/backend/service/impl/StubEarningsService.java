package com.junctionx.backend.service.impl;

import com.junctionx.backend.dto.EarningsDTO.DayCompare;
import com.junctionx.backend.dto.EarningsDTO.RollingAvg;
import com.junctionx.backend.dto.EarningsDTO.RegionCompare;
import com.junctionx.backend.service.EarningsService;
import org.springframework.stereotype.Service;

@Service
public class StubEarningsService implements EarningsService {

    @Override
    public DayCompare dayCompare(String earnerId, String date) {
        double actual = 132.40;
        double predicted = 145.90;
        return new DayCompare(date, actual, predicted);
    }

    @Override
    public RollingAvg rollingAvg(String earnerId, Integer windowDays) {
        int win = (windowDays == null ? 28 : windowDays);
        double actualPerDay = 118.7;
        double predictedPerDay = 129.9;
        return new RollingAvg(win, actualPerDay, predictedPerDay);
    }

    @Override
    public RegionCompare regionCompare(int dayOffset, String regionCode) {
        double actual = 346.1;
        double predicted = 380.6;
        return new RegionCompare(actual, predicted);
    }
}
