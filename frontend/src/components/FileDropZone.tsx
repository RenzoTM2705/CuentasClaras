import { useCallback, useRef, useState } from 'react';

interface Props {
    onFilesSelected: (files: File[]) => void;
}

export default function FileDropZone({ onFilesSelected }: Props) {
    const inputRef = useRef<HTMLInputElement>(null);
    const [isDragging, setIsDragging] = useState(false);

    const handleFiles = useCallback(
        (files: FileList | null) => {
            if (!files) return;
            const excelFiles = Array.from(files).filter((f) =>
                /\.(xlsx|xls)$/i.test(f.name),
            );
            if (excelFiles.length > 0) onFilesSelected(excelFiles);
        },
        [onFilesSelected],
    );

    return (
        <div
            onDragOver={(e) => {
                e.preventDefault();
                setIsDragging(true);
            }}
            onDragLeave={() => setIsDragging(false)}
            onDrop={(e) => {
                e.preventDefault();
                setIsDragging(false);
                handleFiles(e.dataTransfer.files);
            }}
            onClick={() => inputRef.current?.click()}
            className={
                'flex flex-col items-center justify-center gap-3 rounded-2xl border-2 border-dashed cursor-pointer px-8 py-16 text-center transition-colors ' +
                (isDragging
                    ? 'border-brand-500 bg-brand-50'
                    : 'border-slate-300 bg-white hover:border-brand-400')
            }
        >
            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-brand-100 text-brand-600 text-2xl">
                ↑
            </div>
            <div>
                <p className="text-base font-medium text-slate-900">
                    Arrastra tus archivos Excel aquí
                </p>
                <p className="text-sm text-slate-500">
                    o haz clic para seleccionarlos · .xlsx y .xls
                </p>
            </div>
            <input
                ref={inputRef}
                type="file"
                accept=".xlsx,.xls"
                multiple
                className="hidden"
                onChange={(e) => handleFiles(e.target.files)}
            />
        </div>
    );
}