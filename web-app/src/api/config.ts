// URL del backend, senza slash finale. In produzione deve essere HTTPS (es. l'URL di `tailscale serve`).
export const API_BASE_URL = (import.meta.env.VITE_API_URL ?? 'http://localhost:18035').replace(/\/+$/, '');
