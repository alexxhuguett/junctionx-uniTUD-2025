package com.junctionx.backend.utils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.WeekFields;

public class Weeks {
    public static String isoLabel(OffsetDateTime t) {
        var z = t.withOffsetSameInstant(ZoneOffset.UTC).toLocalDate();
        var wf = WeekFields.ISO;
        int week = z.get(wf.weekOfWeekBasedYear());
        int year = z.get(wf.weekBasedYear());
        return "%d-W%02d".formatted(year, week);
    }

    public static OffsetDateTime weekStartUtc(OffsetDateTime t) {
        var z = t.withOffsetSameInstant(ZoneOffset.UTC).toLocalDate()
                .with(WeekFields.ISO.dayOfWeek(), 1); // Monday
        return z.atStartOfDay().atOffset(ZoneOffset.UTC);
    }

    public static OffsetDateTime weekEndUtc(OffsetDateTime t) {
        return weekStartUtc(t).plusWeeks(1); // next Monday 00:00
    }
}
