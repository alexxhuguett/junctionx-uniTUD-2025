// Global, project-wide API you can import anywhere
const _subscribers = new Set();
const _noop = () => console.warn('MapAPI not ready yet — mount <MapPlaceholder /> first.');
export const MapAPI = {
    drawRoute: _noop,
    clearRoute: _noop,
    ready: false,
    /** Subscribe to be notified when MapAPI becomes ready (or changes). Returns an unsubscribe fn. */
    onReady(fn) { _subscribers.add(fn); return () => _subscribers.delete(fn); }
};

import { APIProvider, Map, AdvancedMarker } from '@vis.gl/react-google-maps';
import { GOOGLE_MAPS_API_KEY, MAP_ID } from '../const.jsx';
import ContinuousRouteCreator from "../hooks/ContinuousRouteCreator.jsx";
import DottedRouteCreator from "../hooks/DottedRouteCreator.jsx";
import Alert from "./Alerts/Alert.jsx";
import BreakAlert from "./Alerts/BreakAlert.jsx";
import AcceptRide from "./Alerts/AcceptRide.jsx";
import { forwardRef, useCallback, useImperativeHandle, useMemo, useState, useEffect } from "react";
import useAlerts from "../hooks/hooks.js";



const MapPlaceholder = forwardRef(function MapPlaceholder(_props, ref) {
    const centerPosition = { lat: 60.010338, lng: 30.356907 };

    const { alerts, spawnSurgeAlert, spawnBreakAlert, spawnBonusAlert, spawnAcceptRideAlert} = useAlerts();

    const isBreakAlert = (msg) => msg?.trim().toLowerCase() === 'break alert';
    const isAcceptRideAlert = (msg) => msg?.trim().toLowerCase() === 'ride incoming';

    // Track zoom (kept from original; available if you size markers/cars later)
    const [zoom, setZoom] = useState(18);


    const [currentRoute, setCurrentRoute] = useState(null);

    const drawRoute = useCallback((start, end) => {
        if (!start || !end) return;
        setCurrentRoute({ start, end, key: Date.now() });
    }, []);

    const clearRoute = useCallback(() => setCurrentRoute(null), []);

    useImperativeHandle(ref, () => ({ drawRoute, clearRoute }), [drawRoute, clearRoute]);

    // Expose project-wide API when this component is mounted
    useEffect(() => {
        // wire methods
        MapAPI.drawRoute = drawRoute;
        MapAPI.clearRoute = clearRoute;

        // expose alert creators so other files can add alerts to THIS alerts state
        MapAPI.spawnBreakAlert = () => spawnBreakAlert();
        MapAPI.spawnBonusAlert = () => spawnBonusAlert();
        MapAPI.spawnSurgeAlert  = () => spawnSurgeAlert();
        MapAPI.spawnAcceptRideAlert = () => spawnAcceptRideAlert();

        // mark ready + notify any subscribers waiting for readiness
        MapAPI.ready = true;
        _subscribers.forEach((fn) => fn({ drawRoute, clearRoute }));

        // cleanup on unmount or when deps change
        return () => {
            MapAPI.drawRoute = _noop;
            MapAPI.clearRoute = _noop;

            MapAPI.spawnBreakAlert = undefined;
            MapAPI.spawnBonusAlert = undefined;
            MapAPI.spawnSurgeAlert = undefined;
            MapAPI.spawnAcceptRideAlert = undefined;

            MapAPI.ready = false;
        };
    }, [
        drawRoute,
        clearRoute,
        spawnBreakAlert,
        spawnBonusAlert,
        spawnSurgeAlert,
        spawnAcceptRideAlert,
    ]);


    return (
        <div id="mapContainer">
                <div id="alertContainer" className="alert-stack">
                    {alerts.map((a) =>
                        a.message?.trim().toLowerCase() === "break alert" ? (
                            <BreakAlert key={a.id} onAccept={() => {/*…*/}} onClose={() => {/*…*/}} />
                        ) : a.message?.trim().toLowerCase() === "ride incoming" ? (
                            <AcceptRide key={a.id} onAccept={() => {/*…*/}} onClose={() => {/*…*/}} />
                        ) : (
                            <Alert key={a.id} message={a.message} onClose={() => {/*…*/}} />
                        )
                    )}
                </div>


            <APIProvider apiKey={GOOGLE_MAPS_API_KEY} libraries={['marker', 'routes']}>
                <Map
                    defaultZoom={18}
                    defaultCenter={centerPosition}
                    mapId={MAP_ID}
                    onLoad={(map) => {
                        map.setTilt(45);
                        map.setHeading(30);
                    }}
                    onZoomChanged={(ev) => {
                        const z = ev?.detail?.zoom;
                        if (typeof z === 'number') setZoom(z);
                    }}
                    options={{ disableDefaultUI: true, tilt: 45, heading: 30 }}
                >
                    {/* Center marker */}
                    <AdvancedMarker position={centerPosition} />
                        {currentRoute && (
                        <>
                            {/* Start pin (red tint) */}
                            <AdvancedMarker position={{ lat: currentRoute.start.lat, lng: currentRoute.start.lng }}>
                                <div
                                    style={{
                                        width: 24,
                                        height: 24,
                                        backgroundColor: 'rgba(255, 0, 0, 0.2)',
                                        borderRadius: '50%',
                                        transform: 'translate(-50%, -50%)',
                                    }}
                                />
                            </AdvancedMarker>

                            {/* End pin (green tint) */}
                            <AdvancedMarker position={{ lat: currentRoute.end.lat, lng: currentRoute.end.lng }}>
                                <div
                                    style={{
                                        width: 24,
                                        height: 24,
                                        backgroundColor: 'rgba(0, 255, 0, 0.2)',
                                        borderRadius: '50%',
                                        transform: 'translate(-50%, -50%)',
                                    }}
                                />
                            </AdvancedMarker>

                            {/* Dotted center -> start */}
                            <DottedRouteCreator
                                key={`dotted-${currentRoute.key}`}
                                origin={{ lat: centerPosition.lat, lng: centerPosition.lng }}
                                destination={{ lat: currentRoute.start.lat, lng: currentRoute.start.lng }}
                                travelMode="DRIVING"
                            />

                            {/* Main continuous start -> end */}
                            <ContinuousRouteCreator
                                key={`continuous-${currentRoute.key}`}
                                origin={{ lat: currentRoute.start.lat, lng: currentRoute.start.lng }}
                                destination={{ lat: currentRoute.end.lat, lng: currentRoute.end.lng }}
                                travelMode="DRIVING"
                            />
                        </>
                    )}
                </Map>
            </APIProvider>
        </div>
    );
});

export default MapPlaceholder;
