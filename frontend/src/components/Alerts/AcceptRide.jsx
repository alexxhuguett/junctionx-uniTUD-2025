import AlertCard from "./AlertCard.jsx";

export default function AcceptRide({ onAccept, onClose, pickup, dropoff }) {
    return (
        <AlertCard
            variant="ride"
            title="Ride incoming"
            message={
                pickup && dropoff
                    ? `Pickup: ${pickup}\nDropoff: ${dropoff}`
                    : "A new trip is available."
            }
            acceptLabel="Accept"
            closeLabel="Decline"
            onAccept={onAccept}
            onClose={onClose}
        />
    );
}
