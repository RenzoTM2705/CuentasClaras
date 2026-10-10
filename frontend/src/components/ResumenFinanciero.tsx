import type { ResumenMonetario } from '@/types/analisis';

interface Props {
    resumen: ResumenMonetario;
    compacto?: boolean;
}

function formatearMoneda(valor: number, moneda: 'PEN' | 'USD'): string {
    return new Intl.NumberFormat('es-PE', {
        style: 'currency',
        currency: moneda,
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
    }).format(valor);
}

export default function ResumenFinanciero({ resumen, compacto = false }: Props) {
    if (compacto) {
        return (
            <div className="flex flex-wrap gap-x-4 gap-y-1 text-xs text-slate-600">
                <span>
                    <span className="font-medium text-slate-700">PEN</span>{' '}
                    {formatearMoneda(resumen.totalPEN, 'PEN')}
                </span>
                <span>
                    <span className="font-medium text-slate-700">USD</span>{' '}
                    {formatearMoneda(resumen.totalUSD, 'USD')}
                </span>
            </div>
        );
    }

    return (
        <div className="grid grid-cols-1 gap-3 rounded-lg bg-slate-50 p-3 text-sm md:grid-cols-2">
            <div>
                <div className="text-xs uppercase tracking-wide text-slate-500">
                    Pendiente en soles (PEN)
                </div>
                <div className="mt-1 font-mono text-base font-semibold text-slate-900">
                    {formatearMoneda(resumen.totalPEN, 'PEN')}
                </div>
            </div>
            <div>
                <div className="text-xs uppercase tracking-wide text-slate-500">
                    Pendiente en dólares (USD)
                </div>
                <div className="mt-1 font-mono text-base font-semibold text-slate-900">
                    {formatearMoneda(resumen.totalUSD, 'USD')}
                </div>
            </div>
        </div>
    );
}