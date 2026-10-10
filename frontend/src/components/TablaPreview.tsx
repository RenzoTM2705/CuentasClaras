import { useMemo } from 'react';
import type { VistaGenerada } from '@/types/analisis';

interface Props {
    vista: VistaGenerada;
    maxFilas?: number;
}

function formatearCelda(valor: unknown): string {
    if (valor === null || valor === undefined || valor === '') return '';
    if (typeof valor === 'number') {
        return new Intl.NumberFormat('es-PE', {
            minimumFractionDigits: 0,
            maximumFractionDigits: 2,
        }).format(valor);
    }
    return String(valor);
}

export default function TablaPreview({ vista, maxFilas }: Props) {
    const filas = useMemo(() => {
        if (maxFilas === undefined) return vista.filas;
        return vista.filas.slice(0, maxFilas);
    }, [vista.filas, maxFilas]);

    if (vista.columnas.length === 0) {
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
                        {vista.columnas.map((col, i) => (
                            <th
                                key={i}
                                className="whitespace-nowrap border-b border-slate-200 px-3 py-2 text-left font-semibold text-slate-700"
                            >
                                {col}
                            </th>
                        ))}
                    </tr>
                </thead>
                <tbody>
                    {filas.map((fila, ri) => (
                        <tr key={ri} className="hover:bg-slate-50">
                            {vista.columnas.map((_, ci) => (
                                <td
                                    key={ci}
                                    className="whitespace-nowrap border-b border-slate-100 px-3 py-1.5 text-slate-700"
                                >
                                    {formatearCelda(fila[ci])}
                                </td>
                            ))}
                        </tr>
                    ))}
                </tbody>
            </table>
            {maxFilas !== undefined && vista.filas.length > maxFilas && (
                <div className="border-t border-slate-200 bg-slate-50 px-3 py-2 text-center text-xs text-slate-500">
                    Mostrando {maxFilas} de {vista.filas.length} filas. Amplía para ver todas.
                </div>
            )}
        </div>
    );
}