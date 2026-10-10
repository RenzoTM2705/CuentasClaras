import MosaicoVistas from '@/components/MosaicoVistas';
import type { AnalisisResponse } from '@/types/analisis';

interface Props {
    resultado: AnalisisResponse;
    onVolver: () => void;
}

export default function ResultadosPage({ resultado, onVolver }: Props) {
    const advertencias = resultado.advertencias ?? [];
    const resumen = resultado.resumenCombinacion;

    return (
        <div className="flex h-screen w-screen flex-col bg-slate-100">
            <header className="flex shrink-0 items-center justify-between border-b border-slate-200 bg-white px-6 py-3">
                <div className="flex items-center gap-4">
                    <button
                        type="button"
                        onClick={onVolver}
                        className="flex items-center gap-2 rounded-lg px-3 py-1.5 text-sm font-medium text-slate-600 transition hover:bg-slate-100"
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2} className="h-4 w-4">
                            <path strokeLinecap="round" strokeLinejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                        </svg>
                        Subir más archivos
                    </button>
                    <div className="h-5 w-px bg-slate-200" />
                    <h1 className="text-sm font-semibold text-slate-900">CuentasClaras</h1>
                </div>

                <div className="flex items-center gap-6 text-xs text-slate-600">
                    <span>
                        <span className="font-semibold text-slate-900">{resultado.totalRegistrosValidos}</span>{' '}
                        registros
                    </span>
                    <span>
                        <span className="font-semibold text-slate-900">{resultado.vistas.length}</span>{' '}
                        {resultado.vistas.length === 1 ? 'vista' : 'vistas'}
                    </span>
                    {advertencias.length > 0 && (
                        <span className="text-amber-600">
                            {advertencias.length} {advertencias.length === 1 ? 'advertencia' : 'advertencias'}
                        </span>
                    )}
                </div>
            </header>

            {resumen && (resumen.duplicadosExactos > 0 || resumen.duplicadosParciales > 0) && (
                <div className="shrink-0 border-b border-amber-100 bg-amber-50 px-6 py-2 text-xs text-amber-800">
                    {resumen.duplicadosExactos > 0 && (
                        <span>
                            Se detectaron {resumen.duplicadosExactos}{' '}
                            {resumen.duplicadosExactos === 1 ? 'duplicado exacto' : 'duplicados exactos'}.
                        </span>
                    )}{' '}
                    {resumen.duplicadosParciales > 0 && (
                        <span>
                            {resumen.duplicadosParciales}{' '}
                            {resumen.duplicadosParciales === 1 ? 'registro' : 'registros'} con clave repetida
                            pero valores distintos, marcados para revisión.
                        </span>
                    )}
                </div>
            )}

            <main className="min-h-0 flex-1 p-4">
                {resultado.vistas.length === 0 ? (
                    <div className="flex h-full items-center justify-center text-sm text-slate-500">
                        No se generaron vistas.
                    </div>
                ) : (
                    <MosaicoVistas
                        sessionId={resultado.sessionId}
                        vistas={resultado.vistas}
                        versionAnimacion={1}
                    />
                )}
            </main>
        </div>
    );
}