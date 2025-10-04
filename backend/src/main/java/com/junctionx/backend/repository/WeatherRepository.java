package com.junctionx.backend.repository;

import com.junctionx.backend.model.Weather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, Long> {

    List<Weather> findAllByCityId(Integer cityId);

    void deleteByCityId(Integer cityId);
}
