import type {
    AnalisisResponse,
    ModoAnalisis,
    VistaCompleta,
} from '@/types/analisis';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

export interface HealthResponse {
    status: string;
    service: string;
    timestamp: string;
}

export async function checkHealth(): Promise<HealthResponse> {
    const res = await fetch(`${API_BASE_URL}/api/health`);
    if (!res.ok) throw new Error(`Health check falló: ${res.status}`);
    return res.json();
}

export async function subirArchivos(
    archivos: File[],
    modo: ModoAnalisis,
): Promise<AnalisisResponse> {
    const form = new FormData();
    for (const a of archivos) form.append('archivos', a);
    form.append('modo', modo);

    const res = await fetch(`${API_BASE_URL}/api/analisis`, {
        method: 'POST',
        body: form,
    });

    if (!res.ok) {
        const texto = await res.text().catch(() => '');
        throw new Error(`Error al procesar: ${res.status} ${texto}`);
    }
    return res.json();
}

export async function obtenerVistaCompleta(
    sessionId: string,
    vistaId: string,
): Promise<VistaCompleta> {
    const url = `${API_BASE_URL}/api/sesiones/${encodeURIComponent(sessionId)}/vistas/${encodeURIComponent(vistaId)}`;
    const res = await fetch(url);
    if (!res.ok) {
        const texto = await res.text().catch(() => '');
        throw new Error(`Error al obtener vista completa: ${res.status} ${texto}`);
    }
    return res.json();
}

/** Utilidad: formatea un número como moneda. */
export function formatearMoneda(valor: number, moneda: 'PEN' | 'USD'): string {
    return new Intl.NumberFormat('es-PE', {
        style: 'currency',
        currency: moneda,
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
    }).format(valor);
}