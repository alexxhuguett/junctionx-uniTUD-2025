import { useEffect, useState } from 'react';
import { useMap } from '@vis.gl/react-google-maps';

export function Polyline({ path, strokeColor = '#FF0000', strokeOpacity = 0.8, strokeWeight = 4 }) {
    const map = useMap();
    const [polyline, setPolyline] = useState(null);

    useEffect(() => {
        if (!map) return;
        // Create the polyline when the map is ready
        const pl = new google.maps.Polyline({
            map,
            path,
            strokeColor,
            strokeOpacity,
            strokeWeight,
        });
        setPolyline(pl);
        return () => pl.setMap(null); // cleanup
    }, [map]);

    // Update path or style if props change
    useEffect(() => {
        if (polyline) {
            polyline.setOptions({ path, strokeColor, strokeOpacity, strokeWeight });
        }
    }, [polyline, path, strokeColor, strokeOpacity, strokeWeight]);

    return null;
}
