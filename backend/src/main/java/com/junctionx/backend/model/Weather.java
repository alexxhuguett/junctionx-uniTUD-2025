package com.junctionx.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Daily weather conditions by city.
 * Schema: city_id, date, weather
 */
@Entity
@Table(
        name = "weather_daily",
        indexes = {
                @Index(name = "idx_weather_city_date", columnList = "city_id,date")
        }
)
public class Weather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weather_id")
    private Long id;

    /** City identifier (integer, no foreign key). */
    @Column(name = "city_id", nullable = false)
    private Integer cityId;

    /** Date for which this weather data applies. */
    @Column(name = "date", nullable = false)
    private LocalDate date;

    /**
     * Simplified weather condition (e.g., "Sunny", "Rainy", "Cloudy", "Snowy").
     * You can expand this later to include temperature, precipitation, etc.
     */
    @Column(name = "weather", length = 32, nullable = false)
    private String weather;

    // ========= Constructors =========

    protected Weather() {}

    public Weather(Integer cityId, LocalDate date, String weather) {
        this.cityId = cityId;
        this.date = date;
        this.weather = weather;
    }

    // ========= Getters/Setters =========

    public Long getId() { return id; }

    public Integer getCityId() { return cityId; }
    public void setCityId(Integer cityId) { this.cityId = cityId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getWeather() { return weather; }
    public void setWeather(String weather) { this.weather = weather; }

    // ========= Utility =========

    /** Returns true if the day likely impacts driving (e.g., rain, snow). */
    @Transient
    public boolean isAdverse() {
        if (weather == null) return false;
        String w = weather.toLowerCase();
        return w.contains("rain") || w.contains("snow") || w.contains("storm");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Weather)) return false;
        Weather other = (Weather) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return 31; }
}
