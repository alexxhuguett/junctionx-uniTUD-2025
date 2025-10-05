import React from "react";
import "../../stylesheets/alert.css";

export default function AlertCard({
                                      variant = "info",
                                      title,
                                      message,
                                      onAccept,
                                      onClose,
                                      acceptLabel = "Accept",
                                      closeLabel = "Close",
                                      showAccept = true,
                                      showClose = true,
                                      icon = null,
                                  }) {
    const icons = {
        break: (
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden>
                <path d="M6 3h12v5a4 4 0 0 1-4 4H6V3z" stroke="currentColor" strokeWidth="1.5"/>
                <path d="M4 14h9a4 4 0 0 1 4 4v2H4v-6z" stroke="currentColor" strokeWidth="1.5"/>
            </svg>
        ),
        ride: (
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden>
                <path d="M3 13l2-6h14l2 6" stroke="currentColor" strokeWidth="1.5"/>
                <circle cx="7" cy="17" r="2" stroke="currentColor" strokeWidth="1.5"/>
                <circle cx="17" cy="17" r="2" stroke="currentColor" strokeWidth="1.5"/>
            </svg>
        ),
        info: (
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden>
                <circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="1.5"/>
                <path d="M12 8v.01M11 11h2v5h-2z" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
            </svg>
        ),
    };

    return (
        <div className="alert-card" data-variant={variant} role="dialog" aria-live="polite">
            <div className="alert-inner">
                <div className="alert-icon">{icon || icons[variant] || icons.info}</div>

                {title && <div className="alert-title">{title}</div>}
                {showClose && (
                    <button className="alert-close" onClick={onClose} aria-label={closeLabel}>×</button>
                )}

                {message && <div className="alert-body">{message}</div>}

                <div className="alert-actions">
                    {showClose && (
                        <button className="btn btn-ghost" onClick={onClose}>{closeLabel}</button>
                    )}
                    {showAccept && (
                        <button className="btn btn-primary" onClick={onAccept}>{acceptLabel}</button>
                    )}
                </div>
            </div>
        </div>
    );
}
