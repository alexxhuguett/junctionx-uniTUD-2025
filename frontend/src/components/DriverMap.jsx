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
}