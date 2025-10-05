import {APIProvider, Map, AdvancedMarker, useMap, useMapsLibrary} from '@vis.gl/react-google-maps';
import {useEffect, useMemo, useState} from 'react';
import {PointedPolyline} from "./PointedPolyline.jsx";

export default function DottedRouteCreator({ origin, destination, travelMode = 'DRIVING'}) {
    const map = useMap();
    const routesLib = useMapsLibrary('routes');
    const [path, setPath] = useState([]); // array of {lat, lng}

    useEffect(() => {
        if (!routesLib || !map) return;
        const ds = new routesLib.DirectionsService();
        ds.route({ origin, destination, travelMode }).then(res => {
            const route = res.routes?.[0];
            // overview_path is an array of LatLng; convert to literals
            const pts = route.overview_path.map(ll => ({ lat: ll.lat(), lng: ll.lng() }));
            setPath(pts);
        });
    }, [routesLib, map, origin, destination, travelMode]);

    if (!path.length) return null;
    return (
        <PointedPolyline
            path={path}
            // style options; tweak as you like
            strokeColor={'#000000'}
            strokeOpacity={1}
            strokeWeight={5}
        />
    );
}