// runRouteSequence.js
// Demo runner: sequentially draw a list of routes using the global MapAPI

import { MapAPI } from "../components/MapPlaceholder.jsx";

/** Sleep helper that respects AbortSignal */
export function sleep(ms, signal) {
    return new Promise((resolve, reject) => {
        if (signal?.aborted) return reject(new DOMException("Aborted", "AbortError"));
        const id = setTimeout(() => {
            cleanup(); resolve();
        }, ms);
        function onAbort() { cleanup(); reject(new DOMException("Aborted", "AbortError")); }
        function cleanup() { clearTimeout(id); if (signal) signal.removeEventListener("abort", onAbort); }
        if (signal) signal.addEventListener("abort", onAbort, { once: true });
    });
}

/** Wait until MapAPI is ready (MapPlaceholder mounted), with optional timeout */
export async function waitForMapReady({ timeoutMs = 10000, signal } = {}) {
    if (MapAPI.ready) return;
    await new Promise((resolve, reject) => {
        if (signal?.aborted) return reject(new DOMException("Aborted", "AbortError"));
        let timeoutId;
        const unsubscribe = MapAPI.onReady(() => { cleanup(); resolve(); });
        function onAbort() { cleanup(); reject(new DOMException("Aborted", "AbortError")); }
        function onTimeout() { cleanup(); reject(new Error("Timed out waiting for MapAPI.ready")); }
        function cleanup() {
            clearTimeout(timeoutId);
            unsubscribe && unsubscribe();
            if (signal) signal.removeEventListener("abort", onAbort);
        }
        if (signal) signal.addEventListener("abort", onAbort, { once: true });
        timeoutId = setTimeout(onTimeout, timeoutMs);
        if (MapAPI.ready) { cleanup(); resolve(); } // edge-case race
    });
}

/**
 * Run a sequential demo over a list of routes.
 * @param {Array<{start:{lat:number,lng:number}, end:{lat:number,lng:number}, [meta]:any}>} routes
 * @param {Object} options
 * @param {number} [options.delayMs=1500]  Delay between routes (after processing)
 * @param {number} [options.drawSettleMs=0] Extra wait right after drawing (lets the map render)
 * @param {AbortSignal} [options.signal]    Optional AbortController.signal to cancel
 * @param {(route, index)=>Promise<void>|void} [options.beforeDraw]
 * @param {(route, index)=>Promise<void>|void} [options.afterDraw]
 * @param {(done,total,route)=>void} [options.onProgress]
 * @param {boolean} [options.clearOnFinish=true]
 * @param {number} [options.timeoutMs=10000]
 * @param {boolean} [options.continueOnError=true] If false, the first error aborts the whole run
 */
export async function runRouteSequence(routes, options = {}) {
    const {
        delayMs = 4500,
        drawSettleMs = 0,
        signal,
        beforeDraw,
        afterDraw,
        onProgress,
        clearOnFinish = true,
        timeoutMs = 10000,
        continueOnError = true,
    } = options;

    if (!Array.isArray(routes) || routes.length === 0) return;

    // Ensure MapAPI is wired by MapPlaceholder
    await waitForMapReady({ timeoutMs, signal });

    for (let i = 0; i < routes.length; i++) {
        const route = routes[i];

        if (signal?.aborted) throw new DOMException("Aborted", "AbortError");

        // 1) Before hook
        try {
            if (beforeDraw) await beforeDraw(route, i);
        } catch (err) {
            console.error("beforeDraw error at index", i, err);
            if (!continueOnError) throw err;
        }

        // 2) Draw (support sync or async drawRoute)
        try {
            await Promise.resolve(MapAPI.drawRoute(route.start, route.end));
            if (drawSettleMs > 0) await sleep(drawSettleMs, signal);
        } catch (err) {
            console.error("drawRoute error at index", i, err);
            if (!continueOnError) throw err;
        }

        // 3) Progress callback
        try {
            onProgress && onProgress(i + 1, routes.length, route);
        } catch (err) {
            console.error("onProgress error at index", i, err);
            if (!continueOnError) throw err;
        }

        // 4) After hook
        try {
            if (afterDraw) await afterDraw(route, i);
        } catch (err) {
            console.error("afterDraw error at index", i, err);
            if (!continueOnError) throw err;
        }

        // 5) Delay before next
        if (i < routes.length - 1) {
            await sleep(delayMs, signal);
            MapAPI.clearRoute();
            await sleep(2000, signal);
        }
    }

    if (clearOnFinish && !signal?.aborted) {
        MapAPI.clearRoute();
    }
}

/** Pair two arrays of points into route objects */
export function pairRoutes(starts, ends) {
    const n = Math.min(starts?.length || 0, ends?.length || 0);
    const out = [];
    for (let i = 0; i < n; i++) {
        out.push({
            start: { lat: starts[i].lat, lng: starts[i].lng },
            end: { lat: ends[i].lat, lng: ends[i].lng },
        });
    }
    return out;
}
