package com.junctionx.backend.service;

import com.junctionx.backend.dto.EarningsDTO.DayCompare;
import com.junctionx.backend.dto.EarningsDTO.RollingAvg;
import com.junctionx.backend.dto.EarningsDTO.RegionCompare;

public interface EarningsService {
    DayCompare dayCompare(String earnerId, String date);
    RollingAvg rollingAvg(String earnerId, Integer windowDays);
    RegionCompare regionCompare(int dayOffset, String regionCode);
}
