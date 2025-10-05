import { useEffect, useRef, useState } from "react";
import AcceptRide from "./alerts/AcceptRide.jsx";
import Alert from "./alerts/Alert.jsx";
import BreakAlert from "./alerts/BreakAlert.jsx";
import { runRouteSequence } from "../lib/runRouteSequence.js";
import { MapAPI } from "./MapPlaceholder.jsx";

export default function DecisionDriver() {
    const [ev, setEv] = useState(null);
    const drawCtrlRef = useRef(null);
    const mockTimerRef = useRef(null);

    useEffect(() => {
        const url = new URL(window.location.href);
        const forceMock = url.searchParams.get("mock") === "1";

        if (forceMock) {
            startMock();
            return () => stopMock();
        }

        let opened = false;
        const es = new EventSource("http://localhost:8080/api/stream/decisions");

        // If the stream doesn't open quickly, fall back to mock
        const openGuard = setTimeout(() => {
            if (!opened) {
                try { es.close(); } catch {}
                startMock();
            }
        }, 1500);

        es.onopen = () => {
            opened = true;
            clearTimeout(openGuard);
            // optional: console.log("[SSE] open");
        };

        es.addEventListener("decision", (e) => {
            try {
                const parsed = JSON.parse(e.data); // { type, message, route?, pickupTimeIso }
                handleDecision(parsed);
            } catch (err) {
                console.error("Invalid decision event:", err);
            }
        });

        es.onerror = () => {
            try { es.close(); } catch {}
            if (!opened) startMock();
            // else keep UI idle if stream later errors
        };

        return () => {
            clearTimeout(openGuard);
            try { es.close(); } catch {}
            stopMock();
            if (drawCtrlRef.current) drawCtrlRef.current.abort();
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    function handleDecision(parsed) {
        setEv(parsed);
        if (parsed?.type === "YES" && parsed?.route?.pickup && parsed?.route?.dropoff) {
            drawSingleRoute(parsed.route);
        } else {
            MapAPI?.clearRoutes?.();
        }
    }

    async function drawSingleRoute(route) {
        try {
            if (drawCtrlRef.current) drawCtrlRef.current.abort();
            const controller = new AbortController();
            drawCtrlRef.current = controller;

            const r = {
                start: { lat: Number(route.pickup.lat),  lng: Number(route.pickup.lon)  },
                end:   { lat: Number(route.dropoff.lat), lng: Number(route.dropoff.lon) }
            };

            MapAPI?.clearRoutes?.();

            await runRouteSequence([r], {
                delayMs: 0,
                signal: controller.signal,
                beforeDraw: async () => {},
                afterDraw: async () => {},
                onProgress: () => {},
                clearOnFinish: false
            });
        } catch (err) {
            if (err.name !== "AbortError") console.error(err);
        } finally {
            drawCtrlRef.current = null;
        }
    }

    // -------- MOCK (template) DATA FALLBACK --------
    function startMock() {
        stopMock();
        const templates = [
            {
                type: "YES",
                message: "Take it — high surge nearby.",
                route: {
                    pickup:  { lat: 52.0042, lon: 4.3721 },
                    dropoff: { lat: 52.0128, lon: 4.3879 }
                },
                pickupTimeIso: new Date().toISOString()
            },
            {
                type: "NO",
                message: "Low demand here; wait or reposition.",
                pickupTimeIso: new Date().toISOString()
            },
            {
                type: "BREAK",
                message: "Take a 10 min break.",
                pickupTimeIso: new Date().toISOString()
            },
            {
                type: "YES",
                message: "Great fare estimate — short detour east.",
                route: {
                    pickup:  { lat: 52.0075, lon: 4.3648 },
                    dropoff: { lat: 52.0199, lon: 4.4023 }
                },
                pickupTimeIso: new Date().toISOString()
            }
        ];

        let i = 0;
        const emit = () => {
            const d = templates[i % templates.length];
            handleDecision(d);
            i++;
        };

        emit(); // first immediately
        mockTimerRef.current = setInterval(emit, 6000);
    }

    function stopMock() {
        if (mockTimerRef.current) {
            clearInterval(mockTimerRef.current);
            mockTimerRef.current = null;
        }
    }
    // -----------------------------------------------

    const handleClose = () => setEv(null);
    const handleAccept = () => setEv(null);

    if (!ev) return null;

    if (ev.type === "YES" && ev.route) {
        const pickup = formatCoords(ev.route.pickup);
        const dropoff = formatCoords(ev.route.dropoff);
        return (
            <AcceptRide
                onAccept={handleAccept}
                onClose={handleClose}
                pickup={pickup}
                dropoff={dropoff}
            />
        );
    }

    if (ev.type === "BREAK") {
        return <BreakAlert onAccept={handleAccept} onClose={handleClose} />;
    }

    return <Alert message={ev.message || "No decision."} onClose={handleClose} />;
}

function formatCoords(point) {
    if (!point || typeof point.lat !== "number" || typeof point.lon !== "number") return "";
    const fmt = (n) => Number(n).toFixed(5);
    return `${fmt(point.lat)}, ${fmt(point.lon)}`;
}
