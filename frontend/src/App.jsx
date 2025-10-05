    import { useState } from 'react';
    import { useNavigate } from 'react-router-dom';
    import './stylesheets/App.css';
    import logoUrl from './assets/logo.png';

    export default function App() {
        const [username, setUsername] = useState('');
        const [password, setPassword] = useState('');
        const navigate = useNavigate();

        async function handleSubmit(e) {
            e.preventDefault();

            navigate('/user', { replace: true }); // use replace:false if you want Back to return here
        }

        const canSubmit = username.trim() !== '' && password.trim() !== '';

        return (
            <div id="loginContainer">
                <img src={logoUrl} alt="Logo" className="login-logo"/>
                <form className="login-card" onSubmit={handleSubmit}>
                    <div className="field">
                        <input
                            id="username"
                            autoComplete="username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            placeholder="yourname"
                            required
                        />
                    </div>
                    <div className="field">
                        <input
                            id="password"
                            type="password"
                            autoComplete="current-password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="••••••••"
                            required
                        />
                    </div>
                    <button type="submit" className="login-button" disabled={!canSubmit}>
                        Log in
                    </button>
                </form>
            </div>
        );
    }
