import "../stylesheets/dashboard.css"

import useAlerts from "../hooks/hooks.js"
import '../stylesheets/map.css'
import Alert from "./Alerts/Alert.jsx";
import MapPlaceholder from "./MapPlaceholder.jsx";
import BreakAlert from "./Alerts/BreakAlert.jsx";


export default function Dashboard() {
    const { alerts, spawnSurgeAlert, spawnBreakAlert, spawnBonusAlert } = useAlerts();
    const isBreakAlert = (msg) => msg?.trim().toLowerCase() === 'break alert';

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