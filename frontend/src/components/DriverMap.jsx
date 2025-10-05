import useAlerts from "../hooks/hooks.js"
import '../stylesheets/map.css'
import Alert from "./Alerts/Alert.jsx";
import MapPlaceholder from "./MapPlaceholder.jsx";
import BreakAlert from "./Alerts/BreakAlert.jsx";

export default function DriverMap() {
    const { alerts, spawnSurgeAlert, spawnBreakAlert, spawnBonusAlert } = useAlerts();
    const isBreakAlert = (msg) => msg?.trim().toLowerCase() === 'break alert';

    return (
      <MapPlaceholder/>
    );

    return (
        <div id="map">
            <button onClick={spawnSurgeAlert}>Spawn a Surge Alert</button>
            <button onClick={spawnBreakAlert}>Spawn a Break Alert</button>
            <button onClick={spawnBonusAlert}>Spawn a Bonus Alert</button>

            {alerts.map((a) =>
                isBreakAlert(a.message) ? (
                    <BreakAlert key={a.id}/>           // your special UI
                ) : (
                    <Alert key={a.id} message={a.message} />      // default UI
                )
            )}

        </div>
    );

}