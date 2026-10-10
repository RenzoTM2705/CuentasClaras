import { useEffect, useState } from 'react';
import TablaPreview from './TablaPreview';
import TablaResumenProveedores from './TablaResumenProveedores';
import ResumenFinanciero from './ResumenFinanciero';
import IconoExcel from './IconoExcel';
import { obtenerVistaCompleta } from '@/lib/api';
import type { VistaCompleta, VistaGenerada } from '@/types/analisis';

interface Props {
    sessionId: string;
    vista: VistaGenerada;
    onCerrar: () => void;
}

export default function VistaAmpliada({ sessionId, vista, onCerrar }: Props) {
    const [completa, setCompleta] = useState<VistaCompleta | null>(null);
    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const handler = (e: KeyboardEvent) => {
            if (e.key === 'Escape') onCerrar();
        };
        window.addEventListener('keydown', handler);
        return () => window.removeEventListener('keydown', handler);
    }, [onCerrar]);

    useEffect(() => {
        let cancelado = false;
        setCargando(true);
        setError(null);
        obtenerVistaCompleta(sessionId, vista.id)
            .then((v) => {
                if (!cancelado) setCompleta(v);
            })
            .catch((e) => {
                if (!cancelado) setError(e instanceof Error ? e.message : 'Error desconocido');
            })
            .finally(() => {
                if (!cancelado) setCargando(false);
            });
        return () => {
            cancelado = true;
        };
    }, [sessionId, vista.id]);

    return (
        <div className="fixed inset-0 z-40 flex flex-col bg-slate-50">
            <header className="flex shrink-0 items-center gap-4 border-b border-slate-200 bg-white px-6 py-3">
                <button
                    type="button"
                    onClick={onCerrar}
                    className="flex items-center gap-2 rounded-lg px-3 py-1.5 text-sm font-medium text-slate-600 transition hover:bg-slate-100"
                >
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2} className="h-4 w-4">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                    </svg>
                    Volver al mosaico
                </button>

                <div className="flex min-w-0 flex-1 items-center gap-3">
                    <IconoExcel className="h-6 w-6 shrink-0" />
                    <div className="min-w-0">
                        <div className="truncate text-sm font-semibold text-slate-900">{vista.nombre}</div>
                        <div className="text-xs text-slate-500">
                            {vista.totalDocumentos} {vista.totalDocumentos === 1 ? 'documento' : 'documentos'}
                        </div>
                    </div>
                </div>

                {cargando && (
                    <div className="text-xs text-slate-500">Cargando vista completa…</div>
                )}
            </header>

            <div className="min-h-0 flex-1 overflow-auto">
                {error ? (
                    <div className="p-6">
                        <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-700">
                            {error}
                        </div>
                    </div>
                ) : cargando || !completa ? (
                    <div className="flex h-full items-center justify-center text-sm text-slate-500">
                        Cargando…
                    </div>
                ) : (
                    <div className="space-y-4 p-6">
                        <section>
                            <h2 className="mb-2 text-xs font-semibold uppercase tracking-wide text-slate-500">
                                Resumen general
                            </h2>
                            <ResumenFinanciero resumen={completa.resumen} />
                        </section>

                        <section>
                            <TablaResumenProveedores resumen={completa.resumenPorProveedor ?? []} />
                        </section>

                        <section>
                            <h2 className="mb-2 text-xs font-semibold uppercase tracking-wide text-slate-500">
                                Detalle de documentos
                                <span className="ml-2 text-xs font-normal normal-case text-slate-400">
                                    {completa.totalDocumentos} filas
                                </span>
                            </h2>
                            <TablaCompleta columnas={completa.columnas} filas={completa.filas} />
                        </section>
                    </div>
                )}
            </div>
        </div>
    );
}

/**
 * Tabla de la vista completa. Se separa de TablaPreview porque
 * trabaja con filas completas (no con preview).
 */
function TablaCompleta({
    columnas,
    filas,
}: {
    columnas: string[];
    filas: (string | number | null)[][];
}) {
    const formatearCelda = (valor: unknown): string => {
        if (valor === null || valor === undefined || valor === '') return '';
        if (typeof valor === 'number') {
            return new Intl.NumberFormat('es-PE', {
                minimumFractionDigits: 0,
                maximumFractionDigits: 2,
            }).format(valor);
        }
        return String(valor);
    };

    if (columnas.length === 0) {
        return (
            <div className="rounded-lg border border-slate-200 bg-white p-6 text-center text-sm text-slate-500">
                Esta vista no contiene columnas reconocidas.
            </div>
        );
    }

    return (
        <div className="overflow-auto rounded-lg border border-slate-200 bg-white">
            <table className="min-w-full border-collapse text-xs">
                <thead className="sticky top-0 z-10 bg-slate-100">
                    <tr>
                        {columnas.map((col, i) => (
                            <th key={i} className="whitespace-nowrap border-b border-slate-200 px-3 py-2 text-left font-semibold text-slate-700">
                                {col}
                            </th>
                        ))}
                    </tr>
                </thead>
                <tbody>
                    {filas.map((fila, ri) => (
                        <tr key={ri} className="hover:bg-slate-50">
                            {columnas.map((_, ci) => (
                                <td key={ci} className="whitespace-nowrap border-b border-slate-100 px-3 py-1.5 text-slate-700">
                                    {formatearCelda(fila[ci])}
                                </td>
                            ))}
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}