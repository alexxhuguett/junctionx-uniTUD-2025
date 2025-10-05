import AlertCard from "./AlertCard.jsx";

export default function Alert({ message, onClose }) {
    return (
        <AlertCard
            variant="info"
            title="Heads up"
            message={message}
            acceptLabel="OK"
            showAccept={false}
            onClose={onClose}
        />
    );
}
