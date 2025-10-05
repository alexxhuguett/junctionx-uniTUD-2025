const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

async function http(path, { method = 'GET', headers = {}, body, query } = {}) {
    const url = new URL(path, BASE_URL);
    if (query) Object.entries(query).forEach(([k, v]) => {
        if (v !== undefined && v !== null) url.searchParams.set(k, v);
    });

    const res = await fetch(url.toString(), {
        method,
        headers: { 'Content-Type': 'application/json', ...headers },
        body: body ? JSON.stringify(body) : undefined,
        credentials: 'include', // if you use cookies/sessions; remove if not
    });

    if (!res.ok) {
        const text = await res.text().catch(() => '');
        throw new Error(`${res.status} ${res.statusText} – ${text}`);
    }

    // 204 No Content → return null
    const ct = res.headers.get('content-type') || '';
    return ct.includes('application/json') ? res.json() : null;
}

export { http };
