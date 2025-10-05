import MapPlaceholder from "../components/MapPlaceholder.jsx";
import Dashboard from "../components/Dashboard.jsx";
import DriverMap from "../components/DriverMap.jsx";
import TwoPane from "../components/TwoPane.jsx";
import DemoControls from "../components/DemoControls.jsx";
import DecisionDriver from "../components/DecisionDriver.jsx";
import React from "react";

export default function User() {
    return (
        <>

            <TwoPane left = {<DriverMap />} right = {<Dashboard />}></TwoPane>
            <DecisionDriver/>
        </>
    );
}