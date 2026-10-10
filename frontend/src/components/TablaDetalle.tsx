import { useMemo, useState } from 'react';

interface Props {
    columnas: string[];
    filas: (string | number | null)[][];
}

type Direccion = 'asc' | 'desc' | null;

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

export default function TablaDetalle({ columnas, filas }: Props) {
    const [ordenCol, setOrdenCol] = useState<number | null>(null);
    const [direccion, setDireccion] = useState<Direccion>(null);

    const toggleOrden = (i: number) => {
        if (ordenCol !== i) {
            setOrdenCol(i);
            setDireccion('asc');
            return;
        }
        if (direccion === 'asc') setDireccion('desc');
        else if (direccion === 'desc') {
            setOrdenCol(null);
            setDireccion(null);
        } else setDireccion('asc');
    };

    const filasOrdenadas = useMemo(() => {
        if (ordenCol === null || direccion === null) return filas;
        const copia = [...filas];
        copia.sort((a, b) => {
            const va = a[ordenCol];
            const vb = b[ordenCol];
            if (va === null || va === undefined) return 1;
            if (vb === null || vb === undefined) return -1;
            if (typeof va === 'number' && typeof vb === 'number') {
                return direccion === 'asc' ? va - vb : vb - va;
            }
            const sa = String(va).toLowerCase();
            const sb = String(vb).toLowerCase();
            if (sa < sb) return direccion === 'asc' ? -1 : 1;
            if (sa > sb) return direccion === 'asc' ? 1 : -1;
            return 0;
        });
        return copia;
    }, [filas, ordenCol, direccion]);

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
                            <th
                                key={i}
                                onClick={() => toggleOrden(i)}
                                className="cursor-pointer whitespace-nowrap border-b border-slate-200 px-3 py-2 text-left font-semibold text-slate-700 hover:bg-slate-200"
                                title="Clic para ordenar"
                            >
                                <span className="inline-flex items-center gap-1">
                                    {col}
                                    {ordenCol === i && (
                                        <span className="text-[10px] text-brand-600">
                                            {direccion === 'asc' ? '▲' : direccion === 'desc' ? '▼' : ''}
                                        </span>
                                    )}
                                </span>
                            </th>
                        ))}
                    </tr>
                </thead>
                <tbody>
                    {filasOrdenadas.map((fila, ri) => (
                        <tr key={ri} className="hover:bg-slate-50">
                            {columnas.map((_, ci) => (
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
        </div>
    );
}