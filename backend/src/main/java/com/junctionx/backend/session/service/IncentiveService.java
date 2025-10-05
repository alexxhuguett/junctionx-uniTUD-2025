package com.junctionx.backend.session.service;

import com.junctionx.backend.dto.BonusProgress;
import com.junctionx.backend.repository.IncentiveRepository;
import com.junctionx.backend.repository.JobRepository;
import com.junctionx.backend.utils.Weeks;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class IncentiveService {

    private final IncentiveRepository incentiveRepo;
    private final JobRepository jobRepo;

    public IncentiveService(IncentiveRepository incentiveRepo, JobRepository jobRepo) {
        this.incentiveRepo = incentiveRepo;
        this.jobRepo = jobRepo;
    }

    public BonusProgress getWeeklyProgress(String earnerId, OffsetDateTime asOf) {
        String week = Weeks.isoLabel(asOf);
        var start = Weeks.weekStartUtc(asOf);
        var end   = Weeks.weekEndUtc(asOf);

        int completed = jobRepo.countCompletedInWindow(earnerId, start, end);

        var inc = incentiveRepo.findByEarner_EarnerIdAndWeek(earnerId, week)
                .orElse(null);

        int target = (inc != null && inc.getTargetJobs() != null) ? inc.getTargetJobs() : 0;
        double bonus = (inc != null && inc.getBonusEur() != null) ? inc.getBonusEur() : 0.0;

        boolean achieved = target > 0 && completed >= target;
        int remaining = Math.max(0, target - completed);

        return new BonusProgress(week, completed, target, remaining, achieved, bonus);
    }
}
