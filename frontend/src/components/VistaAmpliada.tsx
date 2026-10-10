import { useEffect, useMemo, useState } from 'react';
import TablaResumenProveedores from './TablaResumenProveedores';
import ResumenFinanciero from './ResumenFinanciero';
import IconoExcel from './IconoExcel';
import FiltrosVista, { FILTROS_VACIOS, type Filtros } from './FiltrosVista';
import TotalesAlPie from './TotalesAlPie';
import TablaDetalle from './TablaDetalle';
import { obtenerVistaCompleta, exportarVista } from '@/lib/api';
import type { VistaCompleta, VistaGenerada } from '@/types/analisis';

interface Props {
    sessionId: string;
    vista: VistaGenerada;
    onCerrar: () => void;
}

function detectarIndiceColumna(columnas: string[], candidatos: string[]): number {
    return columnas.findIndex((c) => {
        const n = c.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '').trim();
        return candidatos.includes(n);
    });
}

export default function VistaAmpliada({ sessionId, vista, onCerrar }: Props) {
    const [completa, setCompleta] = useState<VistaCompleta | null>(null);
    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [filtros, setFiltros] = useState<Filtros>(FILTROS_VACIOS);
    const [exportando, setExportando] = useState(false);
    const [errorExport, setErrorExport] = useState<string | null>(null);

    // Cerrar con Escape
    useEffect(() => {
        const handler = (e: KeyboardEvent) => {
            if (e.key === 'Escape') onCerrar();
        };
        window.addEventListener('keydown', handler);
        return () => window.removeEventListener('keydown', handler);
    }, [onCerrar]);

    // Cargar vista completa
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

    // Índices de columnas relevantes
    const idxProveedor = useMemo(
        () => (completa ? detectarIndiceColumna(completa.columnas, ['proveedor']) : -1),
        [completa],
    );
    const idxRuc = useMemo(
        () => (completa ? detectarIndiceColumna(completa.columnas, ['ruc']) : -1),
        [completa],
    );
    const idxUnidad = useMemo(
        () => (completa ? detectarIndiceColumna(completa.columnas, ['unidad operativa', 'unidad', 'uo']) : -1),
        [completa],
    );
    const idxMoneda = useMemo(
        () => (completa ? detectarIndiceColumna(completa.columnas, ['divisa', 'moneda', 'currency']) : -1),
        [completa],
    );
    const idxSaldoMonedaIngresada = useMemo(
        () => (completa ? detectarIndiceColumna(completa.columnas, ['saldo en moneda ingresada', 'saldo moneda ingresada']) : -1),
        [completa],
    );
    const idxSaldoUSD = useMemo(
        () => (completa ? detectarIndiceColumna(completa.columnas, ['saldo usd', 'saldo en dolares', 'saldo dolares']) : -1),
        [completa],
    );
    const idxSaldoPEN = useMemo(
        () => (completa ? detectarIndiceColumna(completa.columnas, ['saldo pen', 'saldo soles', 'saldo en soles']) : -1),
        [completa],
    );

    // Filas filtradas con su índice original
    const filasFiltradas = useMemo(() => {
        if (!completa) return [];
        const idxs: number[] = [];
        for (let i = 0; i < completa.filas.length; i++) {
            const fila = completa.filas[i];

            if (filtros.proveedor.trim()) {
                const q = filtros.proveedor.toLowerCase().trim();
                const prov = idxProveedor >= 0 ? String(fila[idxProveedor] ?? '').toLowerCase() : '';
                const ruc = idxRuc >= 0 ? String(fila[idxRuc] ?? '').toLowerCase() : '';
                if (!prov.includes(q) && !ruc.includes(q)) continue;
            }

            if (filtros.unidades.length > 0) {
                const u = idxUnidad >= 0 ? String(fila[idxUnidad] ?? '') : '';
                if (!filtros.unidades.includes(u)) continue;
            }

            if (filtros.monedas.length > 0) {
                const m = idxMoneda >= 0 ? String(fila[idxMoneda] ?? '').toUpperCase() : '';
                if (!filtros.monedas.includes(m as 'PEN' | 'USD')) continue;
            }

            idxs.push(i);
        }
        return idxs;
    }, [completa, filtros, idxProveedor, idxRuc, idxUnidad, idxMoneda]);

    const filasVisibles = useMemo(() => {
        if (!completa) return [];
        return filasFiltradas.map((i) => completa.filas[i]);
    }, [completa, filasFiltradas]);

    // Totales filtrados
    const totales = useMemo(() => {
        let pen = 0;
        let usd = 0;
        let pendientes = 0;

        if (!completa) return { pen, usd, pendientes };

        for (const i of filasFiltradas) {
            const fila = completa.filas[i];
            const moneda = idxMoneda >= 0 ? String(fila[idxMoneda] ?? '').toUpperCase() : '';
            const saldoIngresado = idxSaldoMonedaIngresada >= 0 ? Number(fila[idxSaldoMonedaIngresada] ?? 0) : 0;
            const saldoUSD = idxSaldoUSD >= 0 ? Number(fila[idxSaldoUSD] ?? 0) : 0;
            const saldoPEN = idxSaldoPEN >= 0 ? Number(fila[idxSaldoPEN] ?? 0) : 0;

            if (moneda === 'PEN') {
                const v = saldoPEN || saldoIngresado || 0;
                pen += isFinite(v) ? v : 0;
            } else if (moneda === 'USD') {
                const v = saldoUSD || saldoIngresado || 0;
                usd += isFinite(v) ? v : 0;
                if (!saldoPEN && saldoIngresado) pendientes++;
            }
        }

        return { pen, usd, pendientes };
    }, [
        completa,
        filasFiltradas,
        idxMoneda,
        idxSaldoMonedaIngresada,
        idxSaldoUSD,
        idxSaldoPEN,
    ]);

    const handleExportar = async () => {
        if (!completa) return;
        setExportando(true);
        setErrorExport(null);
        try {
            await exportarVista(
                sessionId,
                vista.id,
                {
                    proveedor: filtros.proveedor,
                    unidades: filtros.unidades,
                    monedas: filtros.monedas,
                },
                `${completa.nombre}.xlsx`,
            );
        } catch (e) {
            setErrorExport(e instanceof Error ? e.message : 'Error al exportar');
        } finally {
            setExportando(false);
        }
    };

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

                <button
                    type="button"
                    onClick={handleExportar}
                    disabled={cargando || !completa || exportando}
                    className="flex items-center gap-2 rounded-lg bg-emerald-600 px-3 py-1.5 text-sm font-semibold text-white shadow-sm transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:bg-slate-300"
                >
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2} className="h-4 w-4">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M12 3v12m0 0l-4-4m4 4l4-4M4 17v2a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-2" />
                    </svg>
                    {exportando ? 'Exportando…' : 'Exportar a Excel'}
                </button>
            </header>

            {errorExport && (
                <div className="shrink-0 border-b border-red-100 bg-red-50 px-6 py-2 text-xs text-red-700">
                    {errorExport}
                </div>
            )}

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

                        <FiltrosVista
                            filtros={filtros}
                            unidadesDisponibles={completa.unidadesOperativas}
                            onCambio={setFiltros}
                        />

                        <section>
                            <h2 className="mb-2 text-xs font-semibold uppercase tracking-wide text-slate-500">
                                Detalle de documentos
                                <span className="ml-2 text-xs font-normal normal-case text-slate-400">
                                    {filasVisibles.length} de {completa.totalDocumentos}
                                </span>
                            </h2>
                            <div className="overflow-hidden rounded-lg border border-slate-200">
                                <TablaDetalle columnas={completa.columnas} filas={filasVisibles} />
                                <TotalesAlPie
                                    totalFilasVisibles={filasVisibles.length}
                                    totalFilasOriginales={completa.totalDocumentos}
                                    totalPEN={totales.pen}
                                    totalUSD={totales.usd}
                                    registrosConEquivalenciaPENPendiente={totales.pendientes}
                                />
                            </div>
                        </section>
                    </div>
                )}
            </div>
        </div>
    );
}