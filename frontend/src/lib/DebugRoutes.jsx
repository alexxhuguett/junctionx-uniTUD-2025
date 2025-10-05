// DebugRoutes.jsx
import { useEffect } from 'react';
import { getUser } from './lib/userStore';

export default function DebugRoutes() {
    useEffect(() => {
        const user = getUser();
        const driverId = user?.username;
        if (!driverId) {
            console.log('[DEBUG] No driverId (username not set).');
            return;
        }
        const base = `/api/sessions/${encodeURIComponent(driverId)}`;

        console.log('[DEBUG] Using driverId:', driverId);
        console.log('POST', `${base}/start`);
        console.log('GET ', `${base}`);
        console.log('POST', `${base}/end`);
        console.log('POST', `${base}/location?lat={lat}&lon={lon}&city={city}&hexId9={hex}`);
        console.log('POST', `${base}/break/toggle`);
        console.log('POST', `${base}/break/start`);
        console.log('POST', `${base}/break/end`);
        console.log('POST', `${base}/jobs/{jobId}/complete`);
    }, []);

    return null;
}
