import React, { useState, useEffect, useRef } from "react";
import "../stylesheets/dashboard.css";

export default function Dashboard() {
    const [started, setStarted] = useState(false);
    const [startTime, setStartTime] = useState(null);
    const [elapsed, setElapsed] = useState(0);

    const [onBreak, setOnBreak] = useState(false);
    const [breakStart, setBreakStart] = useState(null);
    const [lastBreakEnd, setLastBreakEnd] = useState(null);
    const [breakElapsed, setBreakElapsed] = useState(0);

    const [money, setMoney] = useState(0.0);
    const [jobsCompleted, setJobsCompleted] = useState(20);
    const [bonusTarget, setBonusTarget] = useState(25);

    const intervalRef = useRef(null);

    const safeCall = (fnName, payload) => {
        try {
            if (typeof window !== "undefined" && typeof window[fnName] === "function") {
                window[fnName](...payload);
            } else {
                console.log(`[telemetry] ${fnName}`, ...payload);
            }
        } catch (e) {
            console.error(`${fnName} hook failed:`, e);
        }
    };

    const handleStart = () => {
        const now = Date.now();
        const isoTime = new Date(now).toISOString();
        safeCall("__sendStartEvent", [{ startTime: isoTime }]);
        setStartTime(now);
        setLastBreakEnd(now);
        setStarted(true);
    };

    const handleBreakToggle = () => {
        const now = Date.now();
        const isoTime = new Date(now).toISOString();

        if (!onBreak) {
            setBreakStart(now);
            setOnBreak(true);
            safeCall("__sendBreakEvent", [{ breakStartTime: isoTime }]);
        } else {
            setOnBreak(false);
            setLastBreakEnd(now);
            safeCall("__sendResumeEvent", [{ resumeTime: isoTime }]);
        }
    };

    const handleStop = () => {
        const now = Date.now();
        const isoTime = new Date(now).toISOString();
        const totalSeconds = startTime ? Math.floor((now - startTime) / 1000) : 0;
        safeCall("__sendStopEvent", [{ stopTime: isoTime, sessionSeconds: totalSeconds }]);
        if (intervalRef.current) clearInterval(intervalRef.current);
        setStarted(false);
        setStartTime(null);
        setElapsed(0);
        setOnBreak(false);
        setBreakStart(null);
        setLastBreakEnd(null);
        setBreakElapsed(0);
    };

    useEffect(() => {
        if (!started) return;
        intervalRef.current = setInterval(() => {
            const now = Date.now();
            setElapsed(Math.floor((now - startTime) / 1000));
            if (onBreak && breakStart) {
                setBreakElapsed(Math.floor((now - breakStart) / 1000));
            } else {
                const baseline = lastBreakEnd ?? startTime;
                setBreakElapsed(Math.floor((now - baseline) / 1000));
            }
        }, 1000);
        return () => clearInterval(intervalRef.current);
    }, [started, startTime, onBreak, breakStart, lastBreakEnd]);

    const formatHHMMSS = (seconds) => {
        const h = String(Math.floor(seconds / 3600)).padStart(2, "0");
        const m = String(Math.floor((seconds % 3600) / 60)).padStart(2, "0");
        const s = String(seconds % 60).padStart(2, "0");
        return `${h}:${m}:${s}`;
    };

    // ---- Time Since Break color thresholds (keep everything else the same) ----
    const ORANGE_TSB = 1.5 * 3600; // 1h 30m
    const RED_TSB = 3 * 3600;      // 3h
    const tsbClass =
        !onBreak && breakElapsed >= RED_TSB
            ? "metric-value tsb-red"
            : !onBreak && breakElapsed >= ORANGE_TSB
                ? "metric-value tsb-orange"
                : "metric-value";

    // Progress percent for Jobs until Bonus
    const bonusPct = Math.max(
        0,
        Math.min(100, Math.round(((bonusTarget || 0) === 0 ? 0 : (jobsCompleted / bonusTarget)) * 100))
    );

    if (!started) {
        return (
            <div className="db-root">
                {/* Centered start state */}
                <div className="db-card db-card--center">
                    <button className="btn btn-start" onClick={handleStart}>Start</button>
                </div>
            </div>
        );
    }

    return (
        <div className="db-root">
            <div className="db-card">
                <div className="stats">
                    <div className="metric">
                        <div className="metric-label">Session</div>
                        <div className="metric-value">{formatHHMMSS(elapsed)}</div>
                    </div>

                    <div className="metric">
                        <div className="metric-label">{onBreak ? "Break time" : "Time Since Break"}</div>
                        <div className={tsbClass}>{formatHHMMSS(breakElapsed)}</div>
                    </div>

                    <div className="metric">
                        <div className="metric-label">Money earned</div>
                        <div className="metric-value">€{money.toFixed(2)}</div>
                    </div>

                    <div className="metric">
                        <div className="metric-label">Jobs until Bonus</div>

                        {/* Progress bar */}
                        <div
                            className="progress"
                            role="progressbar"
                            aria-label="Jobs progress"
                            aria-valuenow={bonusPct}
                            aria-valuemin={0}
                            aria-valuemax={100}
                        >
                            <div className="progress-track">
                                <div
                                    className={`progress-fill ${bonusPct >= 75 ? "progress-fill--complete" : ""}`}
                                    style={{width: `${bonusPct}%`}}
                                />
                            </div>
                        </div>



                        {/* 12/25 below the bar */}
                        <div className="metric-subvalue">
                            {jobsCompleted}/{bonusTarget}
                        </div>
                    </div>
                </div>

                <div className="actions">
                    <button
                        onClick={handleBreakToggle}
                        className={`btn ${onBreak ? "btn-resume" : "btn-break"}`}
                    >
                        {onBreak ? "Resume" : "Break"}
                    </button>

                    <button onClick={handleStop} className="btn btn-stop">
                        Stop
                    </button>
                </div>
            </div>
        </div>
    );
}
