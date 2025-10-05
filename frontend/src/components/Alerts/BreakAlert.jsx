import AlertCard from "./AlertCard.jsx";

export default function BreakAlert({ onAccept, onClose }) {
    return (
        <AlertCard
            variant="break"
            title="Do you want to take a break?"
            message="You’ve been active for a while. A quick 10–15 minute pause can help."
            acceptLabel="Take a break"
            closeLabel="Keep going"
            onAccept={onAccept}
            onClose={onClose}
        />
    );
}
