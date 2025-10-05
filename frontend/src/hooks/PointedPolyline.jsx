import { useEffect, useState } from 'react';
import { useMap } from '@vis.gl/react-google-maps';

export function PointedPolyline({
                                    path,
                                    strokeColor = '#FF0000',
                                    strokeOpacity = 0.9,
                                    strokeWeight = 4,      // not used for the line itself (we hide it), but we can map it to dot size
                                    dotSize = 3,           // px radius of each dot
                                    dotSpacing = '20px',   // distance between dots
                                    offset = '0'           // where the first dot starts
                                }) {
    const map = useMap();
    const [polyline, setPolyline] = useState(null);

    useEffect(() => {
        if (!map) return;

        // Dot symbol
        const dotSymbol = {
            path: google.maps.SymbolPath.CIRCLE,
            scale: dotSize,                 // radius in px
            fillColor: strokeColor,
            fillOpacity: strokeOpacity,
            strokeColor: strokeColor,
            strokeOpacity: strokeOpacity,
            strokeWeight: 1,
        };

        const pl = new google.maps.Polyline({
            map,
            path,
            // Hide the solid stroke; we'll render only the dot pattern
            strokeOpacity: 0,
            icons: [
                {
                    icon: dotSymbol,
                    offset,           // e.g., '0' or '10px' or '10%'
                    repeat: dotSpacing
                }
            ]
        });

        setPolyline(pl);
        return () => pl.setMap(null);
    }, [map]); // create once when map ready

    // Update when props change
    useEffect(() => {
        if (!polyline) return;

        const dotSymbol = {
            path: google.maps.SymbolPath.CIRCLE,
            scale: dotSize,
            fillColor: strokeColor,
            fillOpacity: strokeOpacity,
            strokeColor: strokeColor,
            strokeOpacity: strokeOpacity,
            strokeWeight: 1,
        };

        polyline.setOptions({
            path,
            strokeOpacity: 0,
            icons: [{ icon: dotSymbol, offset, repeat: dotSpacing }]
        });
    }, [polyline, path, strokeColor, strokeOpacity, dotSize, dotSpacing, offset]);

    return null;
}
