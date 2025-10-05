import {APIProvider, Map, AdvancedMarker} from '@vis.gl/react-google-maps';
import {GOOGLE_MAPS_API_KEY, MAP_ID} from '../const.jsx';
import ContinuousRouteCreator from "../hooks/ContinuousRouteCreator.jsx";
import DottedRouteCreator from "../hooks/DottedRouteCreator.jsx";
import CarURL from "../assets/car-svgrepo-com.svg"

export default function MapPlaceholder() {
    const centerPosition = { lat: 60.010338, lng: 30.356907 };
    const numberOfIterations = 2;

    const startPoints = Array.from({ length: numberOfIterations }, (_, i) => {
        const base = centerPosition;
        const latOffset = (Math.random() - 0.5) * 0.03;
        const lngOffset = (Math.random() - 0.5) * 0.02;
        return { id: `s${i + 1}`, lat: base.lat + latOffset, lng: base.lng + lngOffset, title: `start ${i + 1}` };
    });

    const endPoints = Array.from({ length: numberOfIterations }, (_, i) => {
        const base = centerPosition;
        const latOffset = (Math.random() - 0.5) * 0.3;
        const lngOffset = (Math.random() - 0.5) * 0.2;
        return { id: `e${i + 1}`, lat: base.lat + latOffset, lng: base.lng + lngOffset, title: `end ${i + 1}` };
    });

    const routeCount = Math.min(startPoints.length, endPoints.length); // ✅ pair by index safely

    return (
        <APIProvider apiKey={GOOGLE_MAPS_API_KEY} libraries={['marker', 'routes']}> {/* ✅ add 'routes' */}
            <Map
                defaultZoom={18}
                defaultCenter={centerPosition}
                mapId={MAP_ID}
                onLoad={(map) => { map.setTilt(45); map.setHeading(30); }}
                options={{ disableDefaultUI: true, tilt: 45, heading: 30 }}
            >
                {startPoints.map((p) => (
                    <AdvancedMarker key={p.id} position={{ lat: p.lat, lng: p.lng }}>
                        <div
                            style={{
                                width: 24,
                                height: 24,
                                backgroundColor: 'rgba(255, 0, 0, 0.2)',
                                borderRadius: '50%',                    // ✅ camelCase
                                transform: 'translate(-50%, -50%)',
                            }}
                        />
                    </AdvancedMarker>
                ))}

                {endPoints.map((p) => (
                    <AdvancedMarker key={p.id} position={{ lat: p.lat, lng: p.lng }}>
                        <div
                            style={{
                                width: 24,
                                height: 24,
                                backgroundColor: 'rgba(0, 255, 0, 0.2)',
                                borderRadius: '50%',                    // ✅ camelCase
                                transform: 'translate(-50%, -50%)',
                            }}
                        />
                    </AdvancedMarker>
                ))}

                <AdvancedMarker position={centerPosition}>
                    <img
                        src={CarURL}
                        alt=""
                        style={{width: 60, height: 60, translate: 'transform(-50%, -50%)'}}
                    />
                </AdvancedMarker>

                {/* ✅ render routes with map, not forEach; and use RouteCreator */}
                {Array.from({ length: routeCount }).map((_, i) => {
                    const origin = startPoints[i];
                    const destination = endPoints[i];
                    return (
                        <>
                            <ContinuousRouteCreator
                                key={`route-${origin.id}-${destination.id}`}
                                origin={{ lat: origin.lat, lng: origin.lng }}
                                destination={{ lat: destination.lat, lng: destination.lng }}
                                travelMode="DRIVING"
                            />,
                            <DottedRouteCreator
                                key={`route-${0}-${origin.id}`}
                                origin={{ lat: centerPosition.lat, lng: centerPosition.lng }}
                                destination={{ lat: origin.lat, lng: origin.lng }}
                                travelMode="DRIVING"
                            />
                        </>
                    );
                })}
            </Map>
        </APIProvider>
    );
}
