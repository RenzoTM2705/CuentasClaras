import { formatearMoneda } from '@/lib/api';

interface Props {
    totalFilasVisibles: number;
    totalFilasOriginales: number;
    totalPEN: number;
    totalUSD: number;
    registrosConEquivalenciaPENPendiente: number;
}

export default function TotalesAlPie({
    totalFilasVisibles,
    totalFilasOriginales,
    totalPEN,
    totalUSD,
    registrosConEquivalenciaPENPendiente,
}: Props) {
    const filtrado = totalFilasVisibles !== totalFilasOriginales;

    return (
        <div className="sticky bottom-0 z-10 flex flex-wrap items-center justify-between gap-4 border-t border-slate-300 bg-slate-100 px-4 py-2.5 text-xs">
            <div className="text-slate-600">
                {filtrado ? (
                    <>
                        Mostrando <span className="font-semibold text-slate-900">{totalFilasVisibles}</span> de{' '}
                        <span className="font-semibold text-slate-900">{totalFilasOriginales}</span> registros
                    </>
                ) : (
                    <>
                        <span className="font-semibold text-slate-900">{totalFilasVisibles}</span> registros
                    </>
                )}
            </div>

            <div className="flex flex-wrap items-center gap-6">
                <div className="text-slate-600">
                    <span className="mr-1 font-medium">PEN</span>
                    <span className="font-mono font-semibold text-slate-900">
                        {formatearMoneda(totalPEN, 'PEN')}
                    </span>
                </div>
                <div className="text-slate-600">
                    <span className="mr-1 font-medium">USD</span>
                    <span className="font-mono font-semibold text-slate-900">
                        {formatearMoneda(totalUSD, 'USD')}
                    </span>
                </div>
                {registrosConEquivalenciaPENPendiente > 0 && (
                    <div className="text-amber-700">
                        {registrosConEquivalenciaPENPendiente} sin equivalencia PEN
                    </div>
                )}
            </div>
        </div>
    );
}