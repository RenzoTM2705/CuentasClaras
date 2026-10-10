export interface Filtros {
    proveedor: string;
    unidades: string[];
    monedas: ('PEN' | 'USD')[];
}

export const FILTROS_VACIOS: Filtros = {
    proveedor: '',
    unidades: [],
    monedas: [],
};

interface Props {
    filtros: Filtros;
    unidadesDisponibles: string[];
    onCambio: (filtros: Filtros) => void;
}

const MONEDAS: ('PEN' | 'USD')[] = ['PEN', 'USD'];

export default function FiltrosVista({ filtros, unidadesDisponibles, onCambio }: Props) {
    const toggleMoneda = (m: 'PEN' | 'USD') => {
        const tiene = filtros.monedas.includes(m);
        onCambio({
            ...filtros,
            monedas: tiene ? filtros.monedas.filter((x) => x !== m) : [...filtros.monedas, m],
        });
    };

    const toggleUnidad = (u: string) => {
        const tiene = filtros.unidades.includes(u);
        onCambio({
            ...filtros,
            unidades: tiene ? filtros.unidades.filter((x) => x !== u) : [...filtros.unidades, u],
        });
    };

    return (
        <section className="rounded-lg border border-slate-200 bg-white p-4">
            <div className="mb-3 flex items-center justify-between">
                <h2 className="text-sm font-semibold text-slate-800">Filtros</h2>
                <button
                    type="button"
                    onClick={() => onCambio(FILTROS_VACIOS)}
                    className="text-xs font-medium text-brand-600 hover:text-brand-700"
                >
                    Limpiar
                </button>
            </div>

            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                {/* Búsqueda por proveedor o RUC */}
                <div>
                    <label className="mb-1 block text-xs font-medium text-slate-600">
                        Proveedor o RUC
                    </label>
                    <input
                        type="text"
                        value={filtros.proveedor}
                        onChange={(e) => onCambio({ ...filtros, proveedor: e.target.value })}
                        placeholder="Ej. AMERICA MOVIL o 20467534026"
                        className="w-full rounded-md border border-slate-300 px-2.5 py-1.5 text-xs text-slate-700 focus:border-brand-500 focus:outline-none"
                    />
                </div>

                {/* Moneda */}
                <div>
                    <label className="mb-1 block text-xs font-medium text-slate-600">Moneda</label>
                    <div className="flex gap-2">
                        {MONEDAS.map((m) => (
                            <button
                                key={m}
                                type="button"
                                onClick={() => toggleMoneda(m)}
                                className={
                                    'rounded-md px-3 py-1.5 text-xs font-medium transition ' +
                                    (filtros.monedas.includes(m)
                                        ? 'bg-brand-600 text-white'
                                        : 'bg-slate-100 text-slate-600 hover:bg-slate-200')
                                }
                            >
                                {m}
                            </button>
                        ))}
                    </div>
                </div>
            </div>

            {/* Unidades operativas */}
            {unidadesDisponibles.length > 1 && (
                <div className="mt-4">
                    <label className="mb-1 block text-xs font-medium text-slate-600">
                        Unidades operativas
                    </label>
                    <div className="flex flex-wrap gap-1">
                        {unidadesDisponibles.map((u) => (
                            <button
                                key={u}
                                type="button"
                                onClick={() => toggleUnidad(u)}
                                className={
                                    'rounded-md px-2.5 py-1 text-xs font-medium transition ' +
                                    (filtros.unidades.includes(u)
                                        ? 'bg-brand-600 text-white'
                                        : 'bg-slate-100 text-slate-600 hover:bg-slate-200')
                                }
                            >
                                {u}
                            </button>
                        ))}
                    </div>
                </div>
            )}
        </section>
    );
}