package com.junctionx.backend.repository;

import com.junctionx.backend.model.CancellationRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CancellationRateRepository extends JpaRepository<CancellationRate, Long> {

    List<CancellationRate> findAllByCityId(Integer cityId);

    boolean existsByCityIdAndHexagonId9(Integer cityId, String hexagonId9);

    long countByCityId(Integer cityId);

    void deleteByCityId(Integer cityId);
}