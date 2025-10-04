import MapPlaceholder from "../components/MapPlaceholder.jsx";
import Dashboard from "../components/Dashboard.jsx";
import DriverMap from "../components/DriverMap.jsx";
import TwoPane from "../components/TwoPane.jsx";

export default function User() {
    return (
        <TwoPane left = {<DriverMap />} right = {<Dashboard />}></TwoPane>
        // <button></button>
    );
}