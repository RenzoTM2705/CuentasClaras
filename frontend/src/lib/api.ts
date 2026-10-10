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

export interface FiltrosExportacion {
    proveedor?: string;
    unidades?: string[];
    monedas?: ('PEN' | 'USD')[];
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

/**
 * Descarga el .xlsx de una vista aplicando los filtros indicados.
 * El navegador inicia la descarga automáticamente.
 */
export async function exportarVista(
    sessionId: string,
    vistaId: string,
    filtros: FiltrosExportacion = {},
    nombreSugerido = 'vista.xlsx',
): Promise<void> {
    const url = `${API_BASE_URL}/api/sesiones/${encodeURIComponent(sessionId)}/vistas/${encodeURIComponent(vistaId)}/exportar`;

    const res = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(filtros),
    });

    if (!res.ok) {
        const texto = await res.text().catch(() => '');
        throw new Error(`Error al exportar: ${res.status} ${texto}`);
    }

    const blob = await res.blob();

    // Intentar obtener el nombre del header Content-Disposition
    let nombre = nombreSugerido;
    const cd = res.headers.get('Content-Disposition');
    if (cd) {
        const match = cd.match(/filename="?([^";]+)"?/);
        if (match && match[1]) nombre = match[1];
    }

    const urlBlob = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = urlBlob;
    a.download = nombre;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(urlBlob);
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