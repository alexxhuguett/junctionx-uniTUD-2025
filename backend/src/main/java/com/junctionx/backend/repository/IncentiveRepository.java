package com.junctionx.backend.repository;

import com.junctionx.backend.model.IncentiveWeekly;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncentiveRepository extends JpaRepository<IncentiveWeekly, Long> {}
