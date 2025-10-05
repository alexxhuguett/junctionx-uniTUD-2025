// src/components/DemoControls.jsx
import { useRef, useState, useEffect } from "react";
import { runRouteSequence, sleep } from "../lib/runRouteSequence.js";
import { MapAPI } from "./MapPlaceholder.jsx";

export default function DemoControls() {
    const [running, setRunning] = useState(false);
    const ctrlRef = useRef(null);

    useEffect(() => {
        let mounted = true;
        const controller = new AbortController();
        ctrlRef.current = controller;

        (async () => {
            // optional initial delay, cancellable
            await sleep(2000, controller.signal);
            if (!mounted) return;
            if (running) return;

            setRunning(true);

            const routes = [
                { start: { lat: 60.0123, lng: 30.3456 }, end: { lat: 60.0555, lng: 30.4012 } },
                { start: { lat: 60.0101, lng: 30.3200 }, end: { lat: 60.0600, lng: 30.3700 } },
                { start: { lat: 60.0101, lng: 30.3200 }, end: { lat: 60.0600, lng: 30.3700 } },
            ];

            try {
                await runRouteSequence(routes, {
                    delayMs: 6000,
                    signal: controller.signal,
                    beforeDraw: async () => {
                        console.log("before");
                    },
                    afterDraw: async (_route, i) => {
                        console.log("after");
                        // trigger alert INSIDE MapPlaceholder’s alertContainer
                        MapAPI.spawnAcceptRideAlert?.();
                    },
                    onProgress: (done, total) => console.log(`Route ${done}/${total}`),
                    clearOnFinish: true,
                });
            } catch (err) {
                if (err.name !== "AbortError") console.error(err);
            } finally {
                if (mounted) setRunning(false);
                ctrlRef.current = null;
            }
        })();

        return () => {
            mounted = false;
            controller.abort();
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    // no UI? return null. (Or return your Start/Stop buttons.)
    return null;
}
