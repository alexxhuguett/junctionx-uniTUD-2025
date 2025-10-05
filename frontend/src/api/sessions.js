import { http } from './client';

export const sessions = {
    start(earnerId) {
        return http(`/api/sessions/${encodeURIComponent(earnerId)}/start`, { method: 'POST' });
    },
    get(earnerId) {
        return http(`/api/sessions/${encodeURIComponent(earnerId)}`);
    },
    end(earnerId) {
        return http(`/api/sessions/${encodeURIComponent(earnerId)}/end`, { method: 'POST' });
    },
    updateLocation(earnerId, { lat, lng, city, hexId9 }) {
        // ⚠️ backend expects 'lon', not 'lng'
        return http(`/api/sessions/${encodeURIComponent(earnerId)}/location`, {
            method: 'POST',
            query: { lat, lon: lng, city, hexId9 }
        });
    },
    breakToggle(earnerId) {
        return http(`/api/sessions/${encodeURIComponent(earnerId)}/break/toggle`, { method: 'POST' });
    },
    breakStart(earnerId) {
        return http(`/api/sessions/${encodeURIComponent(earnerId)}/break/start`, { method: 'POST' });
    },
    breakEnd(earnerId) {
        return http(`/api/sessions/${encodeURIComponent(earnerId)}/break/end`, { method: 'POST' });
    },
    completeJob(earnerId, jobId) {
        return http(`/api/sessions/${encodeURIComponent(earnerId)}/jobs/${encodeURIComponent(jobId)}/complete`, {
            method: 'POST'
        });
    }
};
