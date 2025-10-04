package com.junctionx.backend.repository;

import com.junctionx.backend.model.HeatMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HeatmapRepository extends JpaRepository<HeatMap, Long> {

    // Fetch all rows that belong to a given heat map
    List<HeatMap> findAllByMapId(String mapId);

    // Quick count for a map
    long countByMapId(String mapId);

    // Guard against duplicates within a map (useful if you keep a unique index)
    boolean existsByMapIdAndHexagonId9(String mapId, String hexagonId9);

    // Bulk delete a whole map’s entries
    void deleteByMapId(String mapId);
}