package com.junctionx.backend.repository;

import com.junctionx.backend.model.IncentiveWeekly;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IncentiveRepository extends JpaRepository<IncentiveWeekly, Long> {
    Optional<IncentiveWeekly> findByEarner_EarnerIdAndWeek(String earnerId, String week);
}
