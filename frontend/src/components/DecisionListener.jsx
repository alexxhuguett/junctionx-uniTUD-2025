import { useEffect } from "react";

// Remove these stubs if you already have real alert functions.
if (typeof window.AlertYes !== "function") {
    window.AlertYes = (message, route, pickupTimeIso) =>
        console.log("[AlertYes]", message, route, pickupTimeIso);
}
if (typeof window.AlertNo !== "function") {
    window.AlertNo = (message) => console.log("[AlertNo]", message);
}
if (typeof window.AlertBreak !== "function") {
    window.AlertBreak = (message, pickupTimeIso) =>
        console.log("[AlertBreak]", message, pickupTimeIso);
}

export default function DecisionListener() {
    useEffect(() => {
        // FE is on :5173, BE is on :8080
        const es = new EventSource("http://localhost:8080/api/stream/decisions");

        es.addEventListener("decision", (e) => {
            const ev = JSON.parse(e.data); // { type, message, route?, pickupTimeIso }

            if (ev.type === "YES" && ev.route) {
                window.AlertYes(ev.message, ev.route, ev.pickupTimeIso);
            } else if (ev.type === "NO") {
                window.AlertNo(ev.message);
            } else if (ev.type === "BREAK") {
                window.AlertBreak(ev.message, ev.pickupTimeIso);
            }
        });

        es.onerror = () => es.close();
        return () => es.close();
    }, []);

    return null; // listener only
}