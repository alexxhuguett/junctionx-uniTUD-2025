import { Link } from 'react-router-dom'
import Alert from "./components/Alerts/Alert.jsx";
import useAlerts from './hooks/hooks.js'

export default function App() {

    const {alerts, spawnBonusAlert, spawnSurgeAlert, spawnBreakAlert} = useAlerts();

    return (
        <div style={{minHeight: '100vh', minWidth: '100%', display: 'flex', flexDirection: 'column'}}>
            <h1>Home</h1>
            <nav style={{marginTop: 120}}>
                <Link to="/user">User</Link>
                <Link to="/dev">Dev</Link>
            </nav>

            <button onClick={alerts.spawnBreakAlert}>Spawn Alert</button>
            <div style={{marginTop: 20}}>
                {alerts.map((a) => (
                    <Alert message = {a.message}/>
                ))}
            </div>
        </div>
    )
}