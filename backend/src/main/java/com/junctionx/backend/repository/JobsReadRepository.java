package com.junctionx.backend.model.repository;

import com.junctionx.backend.sim.dto.TripDTO;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class JobsReadRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public JobsReadRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<TripDTO> ROW = new RowMapper<>() {
        @Override public TripDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            String rideId = rs.getString("ride_id");
            String driverId = rs.getString("driver_id");
            // city_id is integer in DB — we expose it as String in code for uniformity
            Integer cityIdInt = (Integer) rs.getObject("city_id");
            String cityId = cityIdInt == null ? null : String.valueOf(cityIdInt);

            String pickupHex = rs.getString("pickup_hex_id9");
            String dropHex   = rs.getString("drop_hex_id9");

            OffsetDateTime startTs = rs.getObject("start_time", OffsetDateTime.class);
            OffsetDateTime endTs   = rs.getObject("end_time", OffsetDateTime.class);

            Integer durI = (Integer) rs.getObject("duration_mins");
            Double durationMins = durI == null ? null : durI.doubleValue();

            Double fare = (Double) rs.getObject("net_earnings");

            return new TripDTO(
                    rideId, driverId, cityId, pickupHex, dropHex,
                    startTs, endTs, durationMins, fare
            );
        }
    };

    public List<TripDTO> findDriverTripsForDay(String driverId, OffsetDateTime dayStart, OffsetDateTime dayEnd) {
        String sql = """
      SELECT
        j.job_id         AS ride_id,
        j.driver_id      AS driver_id,
        j.city_id        AS city_id,
        j.pickup_hex_id9,
        j.drop_hex_id9,
        j.start_time,
        j.end_time,
        j.duration_mins,
        j.net_earnings
      FROM public.jobs j
      WHERE j.driver_id = :driverId
        AND j.start_time >= :dayStart
        AND j.start_time <  :dayEnd
      ORDER BY j.start_time ASC
    """;
        var params = new MapSqlParameterSource()
                .addValue("driverId", driverId)
                .addValue("dayStart", dayStart)
                .addValue("dayEnd", dayEnd);
        return jdbc.query(sql, params, ROW);
    }

    public List<TripDTO> findCityTripsForDay(Integer cityId, OffsetDateTime dayStart, OffsetDateTime dayEnd) {
        String sql = """
      SELECT
        j.job_id         AS ride_id,
        j.driver_id      AS driver_id,
        j.city_id        AS city_id,
        j.pickup_hex_id9,
        j.drop_hex_id9,
        j.start_time,
        j.end_time,
        j.duration_mins,
        j.net_earnings
      FROM public.jobs j
      WHERE j.city_id = :cityId
        AND j.start_time >= :dayStart
        AND j.start_time <  :dayEnd
      ORDER BY j.start_time ASC
    """;
        var params = new MapSqlParameterSource()
                .addValue("cityId", cityId)
                .addValue("dayStart", dayStart)
                .addValue("dayEnd", dayEnd);
        return jdbc.query(sql, params, ROW);
    }

    public List<TripDTO> findWindowedCandidates(Integer cityId,
                                                OffsetDateTime fromTs,
                                                OffsetDateTime toTs,
                                                List<String> pickupHexes) {
        // NamedParameterJdbcTemplate safely expands IN (:pickupHexes)
        String sql = """
      SELECT
        j.job_id         AS ride_id,
        j.driver_id      AS driver_id,
        j.city_id        AS city_id,
        j.pickup_hex_id9,
        j.drop_hex_id9,
        j.start_time,
        j.end_time,
        j.duration_mins,
        j.net_earnings
      FROM public.jobs j
      WHERE j.city_id = :cityId
        AND j.start_time >= :fromTs
        AND j.start_time <= :toTs
        AND j.pickup_hex_id9 IN (:pickupHexes)
      ORDER BY j.start_time ASC
    """;
        var params = new MapSqlParameterSource()
                .addValue("cityId", cityId)
                .addValue("fromTs", fromTs)
                .addValue("toTs", toTs)
                .addValue("pickupHexes", pickupHexes);
        return jdbc.query(sql, params, ROW);
    }
}
