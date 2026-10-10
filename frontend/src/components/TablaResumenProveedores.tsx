import { useState } from 'react';
import type { ResumenProveedor } from '@/types/analisis';

interface Props {
    resumen: ResumenProveedor[];
}

function formatearMoneda(valor: number, moneda: 'PEN' | 'USD'): string {
    if (valor === 0) return '—';
    return new Intl.NumberFormat('es-PE', {
        style: 'currency',
        currency: moneda,
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
    }).format(valor);
}

export default function TablaResumenProveedores({ resumen }: Props) {
    const [expandido, setExpandido] = useState(false);

    if (resumen.length === 0) {
        return null;
    }

    const visibles = expandido ? resumen : resumen.slice(0, 5);
    const hayMas = resumen.length > 5;

    return (
        <section className="rounded-lg border border-slate-200 bg-white">
            <header className="flex items-center justify-between border-b border-slate-200 px-4 py-2.5">
                <h2 className="text-sm font-semibold text-slate-800">
                    Resumen por proveedor
                    <span className="ml-2 text-xs font-normal text-slate-500">
                        ({resumen.length} {resumen.length === 1 ? 'proveedor' : 'proveedores'})
                    </span>
                </h2>
                {hayMas && (
                    <button
                        type="button"
                        onClick={() => setExpandido((v) => !v)}
                        className="text-xs font-medium text-brand-600 hover:text-brand-700"
                    >
                        {expandido ? 'Ver menos' : `Ver todos (${resumen.length})`}
                    </button>
                )}
            </header>

            <div className="overflow-auto">
                <table className="min-w-full border-collapse text-xs">
                    <thead className="bg-slate-50">
                        <tr>
                            <th className="whitespace-nowrap border-b border-slate-200 px-4 py-2 text-left font-semibold text-slate-600">
                                RUC
                            </th>
                            <th className="whitespace-nowrap border-b border-slate-200 px-4 py-2 text-left font-semibold text-slate-600">
                                Proveedor
                            </th>
                            <th className="whitespace-nowrap border-b border-slate-200 px-4 py-2 text-right font-semibold text-slate-600">
                                Docs
                            </th>
                            <th className="whitespace-nowrap border-b border-slate-200 px-4 py-2 text-right font-semibold text-slate-600">
                                Total PEN
                            </th>
                            <th className="whitespace-nowrap border-b border-slate-200 px-4 py-2 text-right font-semibold text-slate-600">
                                Total USD
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        {visibles.map((r, i) => (
                            <tr key={i} className="hover:bg-slate-50">
                                <td className="whitespace-nowrap border-b border-slate-100 px-4 py-2 font-mono text-slate-700">
                                    {r.ruc ?? '—'}
                                </td>
                                <td className="border-b border-slate-100 px-4 py-2 text-slate-800">
                                    <div className="max-w-md truncate" title={r.proveedor ?? ''}>
                                        {r.proveedor ?? '(sin proveedor)'}
                                    </div>
                                    {r.registrosConEquivalenciaPENPendiente > 0 && (
                                        <div className="mt-0.5 text-[10px] text-amber-600">
                                            {r.registrosConEquivalenciaPENPendiente} sin equivalencia PEN
                                        </div>
                                    )}
                                </td>
                                <td className="whitespace-nowrap border-b border-slate-100 px-4 py-2 text-right text-slate-700">
                                    {r.totalDocumentos}
                                </td>
                                <td className="whitespace-nowrap border-b border-slate-100 px-4 py-2 text-right font-mono text-slate-800">
                                    {formatearMoneda(r.totalPEN, 'PEN')}
                                </td>
                                <td className="whitespace-nowrap border-b border-slate-100 px-4 py-2 text-right font-mono text-slate-800">
                                    {formatearMoneda(r.totalUSD, 'USD')}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </section>
    );
}