import IconoExcel from './IconoExcel';
import ResumenFinanciero from './ResumenFinanciero';
import type { VistaGenerada } from '@/types/analisis';

interface Props {
    vista: VistaGenerada;
    todasLasVistas: VistaGenerada[];
    onAmpliar: () => void;
}

function identificadorDe(v: VistaGenerada): string {
    if (v.tipo === 'archivo') {
        return v.id.startsWith('archivo:') ? v.id.slice('archivo:'.length) : v.nombre;
    }
    if (v.tipo === 'unidad') {
        return v.id.startsWith('unidad:') ? v.id.slice('unidad:'.length) : v.nombre;
    }
    return v.nombre;
}

function construirSubtitulo(vista: VistaGenerada): string {
    if (vista.tipo === 'archivo') {
        return `Archivo individual · ${identificadorDe(vista)}`;
    }
    if (vista.tipo === 'unidad') {
        return `Unidad operativa · ${identificadorDe(vista)}`;
    }
    if (vista.tipo === 'combinado') {
        const unidades = vista.unidadesOperativas ?? [];
        if (unidades.length === 0) return 'Combinado';
        return `Combinado · ${unidades.join(' + ')}`;
    }
    return vista.nombre;
}

export default function RegionVista({ vista, onAmpliar }: Props) {
    const subtitulo = construirSubtitulo(vista);

    return (
        <button
            type="button"
            onClick={onAmpliar}
            className="group relative flex h-full w-full flex-col items-start justify-between rounded-2xl border border-slate-200 bg-white p-5 text-left shadow-sm transition-all hover:-translate-y-0.5 hover:border-brand-400 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-brand-500"
            aria-label={`Ampliar vista ${vista.nombre}`}
        >
            <div className="pointer-events-none absolute right-3 top-3 opacity-0 transition-opacity group-hover:opacity-100 group-focus:opacity-100">
                <div className="flex h-8 w-8 items-center justify-center rounded-full bg-brand-600 text-white shadow-md">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2} className="h-4 w-4">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M4 8V5a1 1 0 0 1 1-1h3M16 4h3a1 1 0 0 1 1 1v3M20 16v3a1 1 0 0 1-1 1h-3M8 20H5a1 1 0 0 1-1-1v-3" />
                    </svg>
                </div>
            </div>

            <div className="flex w-full items-start gap-3">
                <IconoExcel className="mt-0.5 h-10 w-10 shrink-0" />
                <div className="min-w-0 flex-1">
                    <div className="truncate text-base font-semibold text-slate-900" title={vista.nombre}>
                        {vista.nombre}
                    </div>
                    <div className="mt-0.5 truncate text-xs text-slate-500" title={subtitulo}>
                        {subtitulo}
                    </div>
                    <div className="mt-1 text-xs text-slate-500">
                        {vista.totalDocumentos} {vista.totalDocumentos === 1 ? 'documento' : 'documentos'}
                    </div>
                </div>
            </div>

            <div className="mt-4 w-full">
                <ResumenFinanciero resumen={vista.resumen} compacto />
            </div>
        </button>
    );
}