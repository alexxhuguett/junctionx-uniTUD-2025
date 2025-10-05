package com.junctionx.backend.session.service;

import com.junctionx.backend.repository.EarnerRepository;
import com.junctionx.backend.repository.JobRepository;
import com.junctionx.backend.service.RecommendationService;
import com.junctionx.backend.session.SessionLocation;
import com.junctionx.backend.session.UserSession;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class SessionService {

    private static final Duration FATIGUE_THRESHOLD = Duration.ofHours(3);

    private final RecommendationService recommendationService;
    private final EarnerRepository earnerRepo;
    private final JobRepository jobRepo;

    private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();

    public SessionService(RecommendationService recommendationService,
                          EarnerRepository earnerRepo,
                          JobRepository jobRepo) {
        this.recommendationService = recommendationService;
        this.earnerRepo = earnerRepo;
        this.jobRepo = jobRepo;
    }

    public UserSession startSession(String earnerId) {
        earnerRepo.findById(earnerId).orElseThrow(() -> new IllegalArgumentException("Earner not found: " + earnerId));
        if (sessions.containsKey(earnerId)) throw new IllegalStateException("Session already active: " + earnerId);
        UserSession s = new UserSession(earnerId);
        sessions.put(earnerId, s);
        return s;
    }

    public UserSession getOrThrow(String earnerId) {
        var s = sessions.get(earnerId);
        if (s == null) throw new IllegalStateException("No active session for earner " + earnerId);
        return s;
    }

    public void endSession(String earnerId) {
        var s = getOrThrow(earnerId);
        s.finishNow();
        sessions.remove(earnerId);
    }

    public void updateLocation(String earnerId, double lat, double lon, String city, String hexId9) {
        var s = getOrThrow(earnerId);
        s.updateLocation(new SessionLocation(lat, lon, city, hexId9, LocalDateTime.now()));

        // Fatigue reminder only if not on break
        if (!s.isBreakActive() && s.getContinuousDriving().compareTo(FATIGUE_THRESHOLD) >= 0) {
            recommendationService.nudgeBreak(earnerId, FATIGUE_THRESHOLD);
        }
    }

    // --- Break toggle endpoints ---
    public void startBreak(String earnerId) { getOrThrow(earnerId).startBreak(); }
    public void endBreak(String earnerId)   { getOrThrow(earnerId).endBreak(); }
    public void toggleBreak(String earnerId){ getOrThrow(earnerId).toggleBreak(); }

    public void recordJob(String earnerId, String jobId) {
        var s = getOrThrow(earnerId);
        var job = jobRepo.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        double earnings = job.getNetEarnings() != null ? job.getNetEarnings() : 0.0;

        s.recordJob(job.getId(), earnings);

        if (!s.isBreakActive() && s.getContinuousDriving().compareTo(FATIGUE_THRESHOLD) >= 0) {
            recommendationService.nudgeBreak(earnerId, FATIGUE_THRESHOLD);
        }
    }

    // If/when IncentiveService computes bonus €, call:
    public void addBonus(String earnerId, double bonusEur) {
        getOrThrow(earnerId).addBonus(bonusEur); // keeps bonus separate from earnings
    }
}
