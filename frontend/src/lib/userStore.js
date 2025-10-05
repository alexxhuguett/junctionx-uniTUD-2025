const KEY = 'app.user';

export function setUser(user) {
    localStorage.setItem(KEY, JSON.stringify(user));

    console.log(user);
}

export function getUser() {
    try {
        const raw = localStorage.getItem(KEY);
        return raw ? JSON.parse(raw) : null;
    } catch {
        return null;
    }
}

export function clearUser() {
    localStorage.removeItem(KEY);
}
