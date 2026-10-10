import { useEffect, useState } from 'react';
import FileDropZone from '@/components/FileDropZone';
import { checkHealth, subirArchivos } from '@/lib/api';
import type { AnalisisResponse, ModoAnalisis } from '@/types/analisis';

type BackendStatus = 'checking' | 'online' | 'offline';

interface Props {
    onResultado: (r: AnalisisResponse) => void;
}

export default function UploadPage({ onResultado }: Props) {
    const [files, setFiles] = useState<File[]>([]);
    const [backendStatus, setBackendStatus] = useState<BackendStatus>('checking');
    const [modo, setModo] = useState<ModoAnalisis>('combinar');
    const [procesando, setProcesando] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        checkHealth()
            .then(() => setBackendStatus('online'))
            .catch(() => setBackendStatus('offline'));
    }, []);

    const handleFilesSelected = (newFiles: File[]) => {
        setFiles((prev) => {
            const names = new Set(prev.map((f) => f.name));
            const merged = [...prev];
            for (const f of newFiles) if (!names.has(f.name)) merged.push(f);
            return merged;
        });
        setError(null);
    };

    const removeFile = (name: string) => {
        setFiles((prev) => prev.filter((f) => f.name !== name));
    };

    const procesar = async () => {
        if (files.length === 0) return;
        setProcesando(true);
        setError(null);
        try {
            const resultado = await subirArchivos(files, modo);
            onResultado(resultado);
        } catch (e) {
            setError(e instanceof Error ? e.message : 'Error desconocido al procesar');
        } finally {
            setProcesando(false);
        }
    };

    return (
        <div className="min-h-full bg-gradient-to-b from-slate-50 to-white">
            <div className="mx-auto flex max-w-3xl flex-col gap-8 px-6 py-16">
                <header className="space-y-2 text-center">
                    <h1 className="text-4xl font-semibold tracking-tight text-slate-900">
                        CuentasClaras
                    </h1>
                    <p className="text-base text-slate-600">
                        Sube uno o varios reportes Excel de cuentas por pagar para visualizarlos y consolidarlos.
                    </p>
                </header>

                <FileDropZone onFilesSelected={handleFilesSelected} />

                {files.length > 0 && (
                    <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
                        <h2 className="mb-3 text-sm font-medium text-slate-700">
                            Archivos seleccionados ({files.length})
                        </h2>
                        <ul className="space-y-2">
                            {files.map((f) => (
                                <li
                                    key={f.name}
                                    className="flex items-center justify-between rounded-lg bg-slate-50 px-3 py-2"
                                >
                                    <span className="truncate text-sm text-slate-800">{f.name}</span>
                                    <button
                                        type="button"
                                        onClick={() => removeFile(f.name)}
                                        className="ml-4 text-xs font-medium text-slate-500 hover:text-red-600"
                                    >
                                        Quitar
                                    </button>
                                </li>
                            ))}
                        </ul>
                    </section>
                )}

                {files.length > 0 && (
                    <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
                        <h2 className="mb-3 text-sm font-medium text-slate-700">Modo de procesamiento</h2>
                        <div className="flex flex-col gap-2">
                            {(
                                [
                                    ['combinar', 'Unificar todo en una sola vista'],
                                    ['independiente', 'Separar por unidad operativa (o por archivo si no hay unidades)'],
                                    ['ambos', 'Separar por unidad operativa y también mostrar todo unificado'],
                                ] as [ModoAnalisis, string][]
                            ).map(([valor, etiqueta]) => (
                                <label key={valor} className="flex items-center gap-3 text-sm text-slate-700">
                                    <input
                                        type="radio"
                                        name="modo"
                                        value={valor}
                                        checked={modo === valor}
                                        onChange={() => setModo(valor)}
                                        className="h-4 w-4 accent-brand-600"
                                    />
                                    {etiqueta}
                                </label>
                            ))}
                        </div>
                    </section>
                )}

                <div className="flex flex-col items-center gap-3">
                    <button
                        type="button"
                        onClick={procesar}
                        disabled={files.length === 0 || procesando || backendStatus !== 'online'}
                        className="rounded-xl bg-brand-600 px-6 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-slate-300"
                    >
                        {procesando ? 'Procesando…' : 'Procesar archivos'}
                    </button>
                    {error && <p className="text-xs text-red-600">{error}</p>}
                </div>

                <footer className="pt-4 text-center text-xs">
                    <span
                        className={
                            backendStatus === 'online'
                                ? 'text-emerald-600'
                                : backendStatus === 'offline'
                                    ? 'text-red-500'
                                    : 'text-slate-400'
                        }
                    >
                        ● Backend:{' '}
                        {backendStatus === 'online'
                            ? 'conectado'
                            : backendStatus === 'offline'
                                ? 'no disponible'
                                : 'verificando…'}
                    </span>
                </footer>
            </div>
        </div>
    );
}