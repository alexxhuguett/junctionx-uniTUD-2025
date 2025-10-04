package com.junctionx.backend.model;

import com.junctionx.backend.model.enums.ProductType;
import com.junctionx.backend.model.enums.VehicleType;
import com.junctionx.backend.model.enums.FuelType;
import com.junctionx.backend.model.enums.EarnerType;
import com.junctionx.backend.repository.EarnerRepository;
import com.junctionx.backend.repository.HeatmapRepository;
import com.junctionx.backend.repository.IncentiveRepository;
import com.junctionx.backend.repository.JobRepository;
import com.junctionx.backend.repository.SurgeByHourRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Loads mock data from the XLSX on the classpath (app.import.xlsx) into the DB.
 * Sheets used: earners, rides_trips, eats_orders, incentives_weekly, surge_by_hour, heatmap.
 */
@Component
@Profile("import")
public class ExcelDataLoader implements CommandLineRunner {

    @Value("${app.import.xlsx}")
    private String classpathXlsx;

    @Autowired private EarnerRepository earnerRepo;
    @Autowired private JobRepository jobRepo;
    @Autowired private IncentiveRepository incentiveRepo;
    @Autowired private SurgeByHourRepository surgeRepo;
    @Autowired private HeatmapRepository heatmapRepo;

    private static final Logger log = LoggerFactory.getLogger(ExcelDataLoader.class);
    private static final int BATCH = 1000;

    @Override
    public void run(String... args) throws Exception {
        try (InputStream in = new ClassPathResource(classpathXlsx).getInputStream();
             Workbook wb = new XSSFWorkbook(in)) {

            importEarners(wb, "earners");
            importJobs(wb, "rides_trips", ProductType.RIDE);
            importJobs(wb, "eats_orders", ProductType.EATS);
            importIncentives(wb, "incentives_weekly");
            importSurge(wb, "surge_by_hour");
            importHeatmap(wb, "heatmap");
        }
    }

    /* ========================= Earners ========================= */

    private void importEarners(Workbook wb, String sheetName) {
        Sheet sh = wb.getSheet(sheetName);
        if (sh == null) return;

        Map<String,Integer> c = header(sh.getRow(0));
        List<Earner> buf = new ArrayList<>(BATCH);

        for (int i = 1; i <= sh.getLastRowNum(); i++) {
            Row r = sh.getRow(i);
            if (r == null) continue;

            String id = getString(r, c, "earner_id");
            if (isBlank(id)) continue;

            Earner e = new Earner();     // package-scope constructor ok
            e.setEarnerId(id);

            // Optional scalar fields
            Double rating = getDouble(r, c, "rating");
            if (rating != null) {
                try { e.setRating(rating); } catch (Throwable ignored) {}
            }
            Integer homeCity = getInt(r, c, "home_city_id");
            if (homeCity != null) {
                try { e.setHomeCityId(homeCity); } catch (Throwable ignored) {}
            }

            // Enum fields: parse Strings → proper enums before setting
            String vtype = getString(r, c, "vehicle_type");
            if (!isBlank(vtype)) {
                try { e.setVehicleType(parseEnum(VehicleType.class, vtype, null)); } catch (Throwable ignored) {}
            }
            String ftype = getString(r, c, "fuel_type");
            if (!isBlank(ftype)) {
                try { e.setFuelType(parseEnum(FuelType.class, ftype, null)); } catch (Throwable ignored) {}
            }
            String etype = getString(r, c, "earner_type");
            if (!isBlank(etype)) {
                try { e.setEarnerType(parseEnum(EarnerType.class, etype, null)); } catch (Throwable ignored) {}
            }

            buf.add(e);
            if (buf.size() == BATCH) { earnerRepo.saveAll(buf); buf.clear(); }
        }
        if (!buf.isEmpty()) earnerRepo.saveAll(buf);
    }

    /* ====================== Jobs (rides & eats) ====================== */

    /**
     * rides_trips:
     *   id=ride_id, driver=driver_id, requester=rider_id, city_id, product,
     *   start_time, end_time, pickup_lat, pickup_lon, pickup_hex_id9, drop_lat, drop_lon, drop_hex_id9,
     *   distance_km, duration_mins, net_earnings
     *
     * eats_orders:
     *   id=order_id, driver=courier_id, requester=customer_id, city_id,
     *   (no 'product' column → we set "EATS"),
     *   same timing/geo/effort/money columns as rides.
     */
    private void importJobs(Workbook wb, String sheet, ProductType type) {
        Sheet sh = wb.getSheet(sheet);
        if (sh == null) return;

        Map<String,Integer> c = header(sh.getRow(0));
        List<Job> buf = new ArrayList<>(BATCH);

        final boolean isRides = "rides_trips".equalsIgnoreCase(sheet);
        final String colJobId     = isRides ? "ride_id"   : "order_id";
        final String colDriverId  = isRides ? "driver_id" : "courier_id";
        final String colRequester = isRides ? "rider_id"  : "customer_id";

        for (int i = 1; i <= sh.getLastRowNum(); i++) {
            Row r = sh.getRow(i);
            if (r == null) continue;

            String jobId = getString(r, c, colJobId);
            String driverId = getString(r, c, colDriverId);
            String requesterId = getString(r, c, colRequester);
            Integer cityId = getInt(r, c, "city_id");
            LocalDateTime startLdt = getDateTime(r, c, "start_time");
            Double net = getDouble(r, c, "net_earnings");

            // Required by your Job entity (non-nullable fields):
            if (isBlank(jobId) || isBlank(driverId) || isBlank(requesterId) ||
                    cityId == null || startLdt == null || net == null) {
                continue;
            }

            Earner driver = earnerRepo.findById(driverId).orElse(null);
            if (driver == null) continue;

            ZoneOffset startOff = ZoneId.systemDefault().getRules().getOffset(startLdt);
            OffsetDateTime start = startLdt.atOffset(startOff);

            LocalDateTime endLdt = getDateTime(r, c, "end_time");
            OffsetDateTime end = (endLdt == null) ? null
                    : endLdt.atOffset(ZoneId.systemDefault().getRules().getOffset(endLdt));

            // Pickup
            Double pickupLat = getDouble(r, c, "pickup_lat");
            Double pickupLon = getDouble(r, c, "pickup_lon");
            String pickupHex = getString(r, c, "pickup_hex_id9");

            // Drop
            Double dropLat = getDouble(r, c, "drop_lat");
            Double dropLon = getDouble(r, c, "drop_lon");
            String dropHex = getString(r, c, "drop_hex_id9");

            // Effort
            Double distanceKm = getDouble(r, c, "distance_km");
            Integer durationMins = getInt(r, c, "duration_mins");

            // Product label
            String product = isRides ? getString(r, c, "product") : "EATS";
            if (isBlank(product)) product = isRides ? "RIDE" : "EATS";

            // Build via your all-args constructor (no setters used)
            Job j = new Job(
                    jobId,
                    driver,
                    cityId,
                    requesterId,
                    type,
                    product,
                    Boolean.TRUE,      // isCompleted (no status column in sheets)
                    start,
                    end,
                    pickupLat, pickupLon, pickupHex,
                    dropLat,   dropLon,   dropHex,
                    distanceKm, durationMins,
                    net
            );

            buf.add(j);
            if (buf.size() == BATCH) { jobRepo.saveAll(buf); buf.clear(); }
        }
        if (!buf.isEmpty()) jobRepo.saveAll(buf);
    }

    /* ========================= Incentives ========================= */

    private void importIncentives(Workbook wb, String sheet) {
        Sheet sh = wb.getSheet(sheet);
        if (sh == null) return;

        Map<String,Integer> c = header(sh.getRow(0));
        List<IncentiveWeekly> buf = new ArrayList<>(BATCH);

        for (int i = 1; i <= sh.getLastRowNum(); i++) {
            Row r = sh.getRow(i);
            if (r == null) continue;

            String earnerId = getString(r, c, "earner_id");
            LocalDate weekDate = getDate(r, c, "week");
            if (isBlank(earnerId) || weekDate == null) continue;

            Earner e = earnerRepo.findById(earnerId).orElse(null);
            if (e == null) continue;

            IncentiveWeekly iw = new IncentiveWeekly();
            iw.setEarner(e);
            iw.setWeek(weekDate.toString());

            Integer target = getInt(r, c, "target_jobs");
            if (target != null) iw.setTargetJobs(target);
            Integer completed = getInt(r, c, "completed_jobs");
            if (completed != null) iw.setCompletedJobs(completed);
            Double bonus = getDouble(r, c, "bonus_eur");
            if (bonus != null) iw.setAchievedBonus(bonus);

            buf.add(iw);
            if (buf.size() == BATCH) { incentiveRepo.saveAll(buf); buf.clear(); }
        }
        if (!buf.isEmpty()) incentiveRepo.saveAll(buf);
    }

    /* ======================= Surge by hour ======================= */

    private void importSurge(Workbook wb, String sheet) {
        Sheet sh = wb.getSheet(sheet);
        if (sh == null) return;

        Map<String,Integer> c = header(sh.getRow(0));
        List<SurgeByHour> buf = new ArrayList<>(BATCH);

        for (int i = 1; i <= sh.getLastRowNum(); i++) {
            Row r = sh.getRow(i);
            if (r == null) continue;

            Integer city = getInt(r, c, "city_id");
            Integer hour = getInt(r, c, "hour");
            Double mult  = getDouble(r, c, "surge_multiplier");
            if (city == null || hour == null || mult == null) continue;

            SurgeByHour s = new SurgeByHour();
            s.setCityId(city);
            s.setHour(hour);
            s.setSurgeMultiplier(mult);

            buf.add(s);
            if (buf.size() == BATCH) { surgeRepo.saveAll(buf); buf.clear(); }
        }
        if (!buf.isEmpty()) surgeRepo.saveAll(buf);
    }

    /* =========================== Heatmap =========================== */

    /**
     * Uses dotted headers in the sheet:
     *  msg.map_id, msg.city_id, msg.predictions.hexagon_id_9,
     *  msg.predictions.predicted_eph, msg.predictions.predicted_std
     */
    private void importHeatmap(Workbook wb, String sheet) {
        Sheet sh = wb.getSheet(sheet); if (sh == null) return;

        Map<String,Integer> c = header(sh.getRow(0));
        List<HeatMap> buf = new ArrayList<>(BATCH);

        for (int i = 1; i <= sh.getLastRowNum(); i++) {
            Row r = sh.getRow(i); if (r == null) continue;

            String mapId = getString(r, c, "msg.map_id");
            String hex9  = getString(r, c, "msg.predictions.hexagon_id_9");
            if (mapId == null || mapId.isBlank()) continue;   // required
            if (hex9  == null || hex9.isBlank()) continue;     // nullable=false in your entity

            HeatMap h = new HeatMap();      // PK 'id' is auto-generated
            h.setMapId(mapId);
            h.setHexagonId9(hex9);

            Integer city = getInt(r, c, "msg.city_id");
            if (city != null) h.setCityId(city);

            Double eph = getDouble(r, c, "msg.predictions.predicted_eph");
            if (eph != null) h.setPredictedEph(eph);

            Double std = getDouble(r, c, "msg.predictions.predicted_std");
            if (std != null) h.setPredictedStd(std);

            buf.add(h);
            if (buf.size() == BATCH) { heatmapRepo.saveAll(buf); buf.clear(); }
        }
        if (!buf.isEmpty()) heatmapRepo.saveAll(buf);
    }

    /* ============================ Helpers ============================ */

    private static Map<String,Integer> header(Row r){
        Map<String,Integer> m = new HashMap<>();
        if (r == null) return m;
        for (int i = 0; i < r.getLastCellNum(); i++) {
            Cell c = r.getCell(i);
            if (c != null) {
                c.setCellType(CellType.STRING); // deprecated, OK for import utility
                m.put(c.getStringCellValue().trim().toLowerCase(Locale.ROOT), i);
            }
        }
        return m;
    }

    private static String getString(Row r, Map<String,Integer> c, String n){
        Integer i = c.get(n.toLowerCase(Locale.ROOT)); if (i == null) return null;
        Cell x = r.getCell(i); if (x == null) return null;
        x.setCellType(CellType.STRING);
        String s = x.getStringCellValue();
        return s == null ? null : s.trim();
    }

    private static Integer getInt(Row r, Map<String,Integer> c, String n){
        Double d = getDouble(r, c, n);
        return d == null ? null : (int) Math.round(d);
    }

    private static Double getDouble(Row r, Map<String,Integer> c, String n){
        Integer i = c.get(n.toLowerCase(Locale.ROOT)); if (i == null) return null;
        Cell x = r.getCell(i); if (x == null) return null;
        if (x.getCellType() == CellType.NUMERIC) return x.getNumericCellValue();
        try { return Double.parseDouble(x.toString().trim()); } catch (Exception e) { return null; }
    }

    private static LocalDate getDate(Row r, Map<String,Integer> c, String n){
        Integer i = c.get(n.toLowerCase(Locale.ROOT)); if (i == null) return null;
        Cell x = r.getCell(i); if (x == null) return null;
        if (x.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(x))
            return x.getLocalDateTimeCellValue().toLocalDate();
        String s = x.toString().trim();
        if (s.isEmpty()) return null;
        try { return LocalDate.parse(s.substring(0,10)); } catch (Exception ignored) { return null; }
    }

    private static LocalDateTime getDateTime(Row r, Map<String,Integer> c, String n){
        Integer i = c.get(n.toLowerCase(Locale.ROOT)); if (i == null) return null;
        Cell x = r.getCell(i); if (x == null) return null;
        if (x.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(x))
            return x.getLocalDateTimeCellValue();
        String s = x.toString().trim();
        if (s.isEmpty()) return null;
        for (String p : new String[]{"yyyy-MM-dd HH:mm:ss","yyyy-MM-dd'T'HH:mm:ss","yyyy/MM/dd HH:mm"}){
            try { return LocalDateTime.parse(s, DateTimeFormatter.ofPattern(p)); } catch (Exception ignored) {}
        }
        try { return LocalDate.parse(s.substring(0,10)).atStartOfDay(); } catch (Exception ignored) { return null; }
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> e, String v, E fb){
        if (v == null) return fb;
        try { return Enum.valueOf(e, v.trim().toUpperCase(Locale.ROOT)); }
        catch (Exception ex) { return fb; }
    }

    private static boolean isBlank(String s){ return s == null || s.trim().isEmpty(); }
}
