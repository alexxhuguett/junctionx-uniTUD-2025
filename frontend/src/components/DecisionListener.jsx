import { useEffect, useState } from "react";
import AcceptRide from "./alerts/AcceptRide.jsx";
import Alert from "./alerts/Alert.jsx";
import BreakAlert from "./alerts/BreakAlert.jsx";
import {MapAPI} from "./MapPlaceholder.jsx";

export default function DecisionListener() {
    const [ev, setEv] = useState(null); // latest decision event or null

    useEffect(() => {
        const es = new EventSource("http://localhost:8080/api/stream/decisions");

        es.addEventListener("decision", (e) => {
            try {
                const parsed = JSON.parse(e.data); // { type, message, route?, pickupTimeIso }
                setEv(parsed);
            } catch (err) {
                console.error("Invalid decision event:", err);
            }
        });

        es.onerror = () => es.close();
        return () => es.close();
    }, []);

    // No event → render nothing
    if (!ev) return null;

    const handleClose = () => setEv(null);
    const handleAccept = () => setEv(null); // hook your accept action here if needed

    // YES → show AcceptRide with pickup/dropoff strings
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

    // BREAK → show BreakAlert
    if (ev.type === "BREAK") {
        return <BreakAlert onAccept={handleAccept} onClose={handleClose} />;
    }

    // NO (or fallback) → simple info Alert
    return <Alert message={ev.message || "No decision."} onClose={handleClose} />;
}

function formatCoords(point) {
    if (!point || typeof point.lat !== "number" || typeof point.lon !== "number") return "";
    const fmt = (n) => Number(n).toFixed(5);
    return `${fmt(point.lat)}, ${fmt(point.lon)}`;
}
