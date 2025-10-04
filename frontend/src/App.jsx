import { Link } from 'react-router-dom'

export default function App() {
    return (
        <div style={{ padding: 20 }}>
            <h1>Home</h1>
            <nav style={{ marginTop: 12 }}>
                <Link to="/user" style={{ marginRight: 10 }}>User</Link>
                <Link to="/dev">Dev</Link>
            </nav>
        </div>
    )
}