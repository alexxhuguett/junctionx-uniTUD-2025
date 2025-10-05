// import {APIProvider, Map, AdvancedMarker} from '@vis.gl/react-google-maps';
// import {GOOGLE_MAPS_API_KEY, MAP_ID} from '../const.jsx';
// import ContinuousRouteCreator from "../hooks/ContinuousRouteCreator.jsx";
// import DottedRouteCreator from "../hooks/DottedRouteCreator.jsx";
// import CarURL from "../assets/navigation-arrow-fill-svgrepo-com.svg"
// import Alert from "./Alerts/Alert.jsx";
// import useAlerts from "../hooks/hooks.js";
// import BreakAlert from "./Alerts/BreakAlert.jsx";
// import AcceptRide from "./Alerts/AcceptRide.jsx";
//
// export default function MapPlaceholder() {
//     const centerPosition = { lat: 60.010338, lng: 30.356907 };
//     const numberOfIterations = 2;
//
//     const { alerts, spawnSurgeAlert, spawnBreakAlert, spawnBonusAlert } = useAlerts();
//     const isBreakAlert = (msg) => msg?.trim().toLowerCase() === 'break alert';
//     const isAcceptRideAlert = (msg) => msg?.trim().toLowerCase() === 'ride incoming';
//
//     const startPoints = Array.from({ length: numberOfIterations }, (_, i) => {
//         const base = centerPosition;
//         const latOffset = (Math.random() - 0.5) * 0.03;
//         const lngOffset = (Math.random() - 0.5) * 0.02;
//         return { id: `s${i + 1}`, lat: base.lat + latOffset, lng: base.lng + lngOffset, title: `start ${i + 1}` };
//     });
//
//     const endPoints = Array.from({ length: numberOfIterations }, (_, i) => {
//         const base = centerPosition;
//         const latOffset = (Math.random() - 0.5) * 0.3;
//         const lngOffset = (Math.random() - 0.5) * 0.2;
//         return { id: `e${i + 1}`, lat: base.lat + latOffset, lng: base.lng + lngOffset, title: `end ${i + 1}` };
//     });
//
//     const routeCount = Math.min(startPoints.length, endPoints.length);
//
//     return (
//         <div id="mapContainer">
//             <div id = "alertContainer">
//                 {alerts.map((a) =>
//                     isBreakAlert(a.message) ? (
//                         <BreakAlert key={a.id}/>           // your special UI
//                     ) : ( isAcceptRideAlert(a.message) ? (
//                         <AcceptRide key={a.id}/>) :
//                         (<Alert key={a.id} message={a.message} />)      // default UI
//                     )
//                 )}
//             </div>
//             <APIProvider apiKey={GOOGLE_MAPS_API_KEY} libraries={['marker', 'routes']}>
//                 <Map
//                     defaultZoom={18}
//                     defaultCenter={centerPosition}
//                     mapId={MAP_ID}
//                     onLoad={(map) => {
//                         map.setTilt(45);
//                         map.setHeading(30);
//                     }}
//                     options={{disableDefaultUI: true, tilt: 45, heading: 30}}
//                 >
//                     {startPoints.map((p) => (
//                         <AdvancedMarker key={p.id} position={{lat: p.lat, lng: p.lng}}>
//                             <div
//                                 style={{
//                                     width: 24,
//                                     height: 24,
//                                     backgroundColor: 'rgba(255, 0, 0, 0.2)',
//                                     borderRadius: '50%',
//                                     transform: 'translate(-50%, -50%)',
//                                 }}
//                             />
//                         </AdvancedMarker>
//                     ))}
//
//                     {endPoints.map((p) => (
//                         <AdvancedMarker key={p.id} position={{lat: p.lat, lng: p.lng}}>
//                             <div
//                                 style={{
//                                     width: 24,
//                                     height: 24,
//                                     backgroundColor: 'rgba(0, 255, 0, 0.2)',
//                                     borderRadius: '50%',
//                                     transform: 'translate(-50%, -50%)',
//                                 }}
//                             />
//                         </AdvancedMarker>
//                     ))}
//
//                     <AdvancedMarker position={centerPosition}>
//                         <img
//                             src={CarURL}
//                             alt=""
//                             style={{width: 60, height: 60, translate: 'transform(-50%, -50%)'}}
//                         />
//                     </AdvancedMarker>
//
//                     {Array.from({length: routeCount}).map((_, i) => {
//                         const origin = startPoints[i];
//                         const destination = endPoints[i];
//                         return (
//                             [
//                                 <ContinuousRouteCreator
//                                     key={`route-${origin.id}-${destination.id}`}
//                                     origin={{lat: origin.lat, lng: origin.lng}}
//                                     destination={{lat: destination.lat, lng: destination.lng}}
//                                     travelMode="DRIVING"
//                                 />,
//                                 <DottedRouteCreator
//                                     key={`route-${0}-${origin.id}`}
//                                     origin={{lat: centerPosition.lat, lng: centerPosition.lng}}
//                                     destination={{lat: origin.lat, lng: origin.lng}}
//                                     travelMode="DRIVING"
//                                 />
//                             ]
//                         );
//                     })}
//                 </Map>
//             </APIProvider>
//
//
//         </div>
//     );
// }
import { useState, useMemo } from "react";
import {APIProvider, Map, AdvancedMarker} from '@vis.gl/react-google-maps';
import {GOOGLE_MAPS_API_KEY, MAP_ID} from '../const.jsx';
import ContinuousRouteCreator from "../hooks/ContinuousRouteCreator.jsx";
import DottedRouteCreator from "../hooks/DottedRouteCreator.jsx";
import CarURL from "../assets/navigation-arrow-fill-svgrepo-com.svg"
import Alert from "./Alerts/Alert.jsx";
import useAlerts from "../hooks/hooks.js";
import BreakAlert from "./Alerts/BreakAlert.jsx";
import AcceptRide from "./Alerts/AcceptRide.jsx";

export default function MapPlaceholder() {
    const centerPosition = { lat: 60.010338, lng: 30.356907 };
    const numberOfIterations = 2;

    const { alerts, spawnSurgeAlert, spawnBreakAlert, spawnBonusAlert } = useAlerts();
    const isBreakAlert = (msg) => msg?.trim().toLowerCase() === 'break alert';
    const isAcceptRideAlert = (msg) => msg?.trim().toLowerCase() === 'ride incoming';

    // Track zoom (for car size)
    const [zoom, setZoom] = useState(18);
    const carSize = Math.round(Math.min(72, Math.max(16, 48 * (zoom / 18))));

    // ✅ Generate random start/end points ONCE so they don't change on re-render/zoom
    const startPoints = useMemo(() => {
        return Array.from({ length: numberOfIterations }, (_, i) => {
            const base = centerPosition;
            const latOffset = (Math.random() - 0.5) * 0.03;
            const lngOffset = (Math.random() - 0.5) * 0.02;
            return { id: `s${i + 1}`, lat: base.lat + latOffset, lng: base.lng + lngOffset, title: `start ${i + 1}` };
        });
    }, []); // ← no deps: create once

    const endPoints = useMemo(() => {
        return Array.from({ length: numberOfIterations }, (_, i) => {
            const base = centerPosition;
            const latOffset = (Math.random() - 0.5) * 0.3;
            const lngOffset = (Math.random() - 0.5) * 0.2;
            return { id: `e${i + 1}`, lat: base.lat + latOffset, lng: base.lng + lngOffset, title: `end ${i + 1}` };
        });
    }, []); // ← create once

    const routeCount = Math.min(startPoints.length, endPoints.length);

    return (
        <div id="mapContainer">
            <div id="alertContainer">
                {alerts.map((a) =>
                    isBreakAlert(a.message) ? (
                        <BreakAlert key={a.id}/>
                    ) : ( isAcceptRideAlert(a.message) ? (
                                <AcceptRide key={a.id}/>) :
                            (<Alert key={a.id} message={a.message} />)
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
                    options={{disableDefaultUI: true, tilt: 45, heading: 30}}
                >
                    {startPoints.map((p) => (
                        <AdvancedMarker key={p.id} position={{lat: p.lat, lng: p.lng}}>
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
                    ))}

                    {endPoints.map((p) => (
                        <AdvancedMarker key={p.id} position={{lat: p.lat, lng: p.lng}}>
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
                    ))}

                    <AdvancedMarker position={centerPosition}></AdvancedMarker>

                    {Array.from({length: routeCount}).map((_, i) => {
                        const origin = startPoints[i];
                        const destination = endPoints[i];
                        return ([
                            <ContinuousRouteCreator
                                key={`route-${origin.id}-${destination.id}`}
                                origin={{lat: origin.lat, lng: origin.lng}}
                                destination={{lat: destination.lat, lng: destination.lng}}
                                travelMode="DRIVING"
                            />,
                            <DottedRouteCreator
                                key={`route-${0}-${origin.id}`}
                                origin={{lat: centerPosition.lat, lng: centerPosition.lng}}
                                destination={{lat: origin.lat, lng: origin.lng}}
                                travelMode="DRIVING"
                            />
                        ]);
                    })}
                </Map>
            </APIProvider>
        </div>
    );
}
