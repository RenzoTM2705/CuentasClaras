import { useEffect, useState } from 'react';
import FileDropZone from '@/components/FileDropZone';
import { checkHealth } from '@/lib/api';

type Status = 'checking' | 'online' | 'offline';

export default function UploadPage() {
    const [files, setFiles] = useState<File[]>([]);
    const [status, setStatus] = useState<Status>('checking');

    useEffect(() => {
        checkHealth()
            .then(() => setStatus('online'))
            .catch(() => setStatus('offline'));
    }, []);

    const addFiles = (newFiles: File[]) => {
        setFiles((prev) => {
            const names = new Set(prev.map((f) => f.name));
            const merged = [...prev];
            for (const f of newFiles) if (!names.has(f.name)) merged.push(f);
            return merged;
        });
    };

    return (
        <div className="min-h-full bg-gradient-to-b from-slate-50 to-white">
            <div className="mx-auto flex max-w-3xl flex-col gap-8 px-6 py-16">
                <header className="space-y-2 text-center">
                    <h1 className="text-4xl font-semibold tracking-tight text-slate-900">
                        CuentasClaras
                    </h1>
                    <p className="text-base text-slate-600">
                        Sube uno o varios reportes Excel de cuentas por pagar.
                    </p>
                </header>

                <FileDropZone onFilesSelected={addFiles} />

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
                                        onClick={() =>
                                            setFiles((prev) => prev.filter((x) => x.name !== f.name))
                                        }
                                        className="ml-4 text-xs font-medium text-slate-500 hover:text-red-600"
                                    >
                                        Quitar
                                    </button>
                                </li>
                            ))}
                        </ul>
                    </section>
                )}

                <div className="flex flex-col items-center gap-3">
                    <button
                        disabled={files.length === 0}
                        className="rounded-xl bg-brand-600 px-6 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-slate-300"
                    >
                        Procesar archivos
                    </button>
                    <p className="text-xs text-slate-500">
                        El procesamiento real se implementará en la Fase 3.
                    </p>
                </div>

                <footer className="pt-4 text-center text-xs">
                    <span
                        className={
                            status === 'online'
                                ? 'text-emerald-600'
                                : status === 'offline'
                                    ? 'text-red-500'
                                    : 'text-slate-400'
                        }
                    >
                        ● Backend:{' '}
                        {status === 'online'
                            ? 'conectado'
                            : status === 'offline'
                                ? 'no disponible'
                                : 'verificando…'}
                    </span>
                </footer>
            </div>
        </div>
    );
}