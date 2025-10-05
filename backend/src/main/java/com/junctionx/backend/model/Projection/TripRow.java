package com.junctionx.backend.model.projection;

import java.time.OffsetDateTime;

public interface TripRow {
    String getRideId();         // job_id
    String getDriverId();       // driver_id
    String getCityId();         // city_id (cast to text in SQL)
    String getPickupHexId9();   // pickup_hex_id9
    String getDropoffHexId9();  // drop_hex_id9
    OffsetDateTime getStartTs(); // start_time (timestamptz)
    OffsetDateTime getEndTs();   // end_time (timestamptz)
    Double getDurationMins();    // duration_mins (int in DB -> cast to double)
    Double getFare();            // net_earnings
}
