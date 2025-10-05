package com.junctionx.backend.repository;

import com.junctionx.backend.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface JobRepository extends JpaRepository<Job, String> {
    @Query("""
        SELECT COUNT(j) FROM Job j
        WHERE j.driver.earnerId = :earnerId
          AND j.isCompleted = TRUE
          AND j.startTime >= :start
          AND j.startTime < :end
    """)
    int countCompletedInWindow(@Param("earnerId") String earnerId,
                               @Param("start") OffsetDateTime start,
                               @Param("end") OffsetDateTime end);
}