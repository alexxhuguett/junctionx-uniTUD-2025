import { useState, useCallback } from "react";

export default function useAlerts() {
    const [alerts, setAlerts] = useState([]);

    // helper to spawn any alert and auto-remove after 5s
    const spawnAlert = useCallback((message) => {
        const id = Date.now();
        setAlerts(prev => [...prev, { id, message }]);

        // remove this alert after 5 seconds
        setTimeout(() => {
            setAlerts(prev => prev.filter(a => a.id !== id));
        }, 5000);
    }, []);

    // convenience wrappers
    const spawnBonusAlert = useCallback(() => {
        spawnAlert("If you keep it up you get a 200 euro bonus");
    }, [spawnAlert]);

    const spawnSurgeAlert = useCallback(() => {
        spawnAlert("Surge in 15 mins in Galati");
    }, [spawnAlert]);

    const spawnBreakAlert = useCallback(() => {
        spawnAlert("Take a break");
    }, [spawnAlert]);

    return { alerts, spawnBonusAlert, spawnSurgeAlert, spawnBreakAlert };
}
