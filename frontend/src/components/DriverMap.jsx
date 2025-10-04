import useAlerts from "../hooks/hooks.js"
import '../stylesheets/map.css'
import Alert from "./Alerts/Alert.jsx";
import MapPlaceholder from "./MapPlaceholder.jsx";

export default function DriverMap() {
    const { alerts, spawnSurgeAlert, spawnBreakAlert, spawnBonusAlert } = useAlerts();

    return (
      <MapPlaceholder/>
    );

    // return (
    //     <div id="map">
    //         <button onClick={spawnSurgeAlert}>Spawn a Surge Alert</button>
    //         <button onClick={spawnBreakAlert}>Spawn a Break Alert</button>
    //         <button onClick={spawnBonusAlert}>Spawn a Bonus Alert</button>
    //
    //          The alerts are now part of the same div
    //         {alerts.map((a, i) => (
    //             <Alert key={i} message={a.message} />
    //         ))}
    //     </div>
    // );

}