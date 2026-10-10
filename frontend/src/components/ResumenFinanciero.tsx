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
                {resumen.registrosConEquivalenciaPENPendiente > 0 && (
                    <span className="text-amber-600">
                        {resumen.registrosConEquivalenciaPENPendiente} sin equivalencia PEN
                    </span>
                )}
            </div>
        );
    }

    return (
        <div className="grid grid-cols-2 gap-3 rounded-lg bg-slate-50 p-3 text-sm md:grid-cols-4">
            <div>
                <div className="text-xs uppercase tracking-wide text-slate-500">Total PEN</div>
                <div className="mt-1 font-mono text-base font-semibold text-slate-900">
                    {formatearMoneda(resumen.totalPEN, 'PEN')}
                </div>
            </div>
            <div>
                <div className="text-xs uppercase tracking-wide text-slate-500">Total USD</div>
                <div className="mt-1 font-mono text-base font-semibold text-slate-900">
                    {formatearMoneda(resumen.totalUSD, 'USD')}
                </div>
            </div>
            <div>
                <div className="text-xs uppercase tracking-wide text-slate-500">
                    Funcional PEN confirmado
                </div>
                <div className="mt-1 font-mono text-base font-semibold text-slate-900">
                    {formatearMoneda(resumen.totalFuncionalPENConfirmado, 'PEN')}
                </div>
            </div>
            <div>
                <div className="text-xs uppercase tracking-wide text-slate-500">
                    Equivalencia PEN pendiente
                </div>
                <div className="mt-1 font-mono text-base font-semibold text-amber-600">
                    {resumen.registrosConEquivalenciaPENPendiente}
                </div>
            </div>
        </div>
    );
}