import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import App from './App.jsx'
import User from './pages/User.jsx'
import Dev from './pages/Dev.jsx'
import './index.css'

createRoot(document.getElementById('root')).render(
    <StrictMode>
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<App />} />
                <Route path="/user" element={<User />} />
                <Route path="/dev" element={<Dev />} />
            </Routes>
        </BrowserRouter>
    </StrictMode>
)