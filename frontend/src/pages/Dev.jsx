import TwoPane from "../components/TwoPane.jsx";
import DevMap from "../components/DevMap.jsx";
import Dashboard from "../components/Dashboard.jsx";

export default function Dev() {
    return (
        <TwoPane left ={<DevMap />} right = {<Dashboard />}></TwoPane>
    );
}