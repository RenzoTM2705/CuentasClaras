import { useEffect, useState } from 'react';
import RegionVista from './RegionVista';
import VistaAmpliada from './VistaAmpliada';
import type { VistaGenerada } from '@/types/analisis';

interface Props {
    sessionId: string;
    vistas: VistaGenerada[];
    versionAnimacion: number;
}

function calcularGrid(total: number): { cols: number; rows: number } {
    if (total <= 1) return { cols: 1, rows: 1 };
    if (total === 2) return { cols: 2, rows: 1 };
    if (total === 3) return { cols: 3, rows: 1 };
    if (total === 4) return { cols: 2, rows: 2 };
    if (total <= 6) return { cols: 3, rows: 2 };
    if (total <= 9) return { cols: 3, rows: 3 };
    return { cols: 4, rows: Math.ceil(total / 4) };
}

export default function MosaicoVistas({ sessionId, vistas, versionAnimacion }: Props) {
    const [ampliada, setAmpliada] = useState<VistaGenerada | null>(null);
    const [animando, setAnimando] = useState(false);

    useEffect(() => {
        setAnimando(true);
        const t = setTimeout(() => setAnimando(false), 800);
        return () => clearTimeout(t);
    }, [versionAnimacion]);

    const { cols, rows } = calcularGrid(vistas.length);

    return (
        <>
            <div
                className="grid h-full w-full gap-4"
                style={{
                    gridTemplateColumns: `repeat(${cols}, minmax(0, 1fr))`,
                    gridTemplateRows: `repeat(${rows}, minmax(0, 1fr))`,
                }}
            >
                {vistas.map((v, i) => (
                    <div
                        key={v.id}
                        className={animando ? 'mosaico-animacion' : ''}
                        style={animando ? { animationDelay: `${i * 60}ms` } : undefined}
                    >
                        <RegionVista
                            vista={v}
                            todasLasVistas={vistas}
                            onAmpliar={() => setAmpliada(v)}
                        />
                    </div>
                ))}
            </div>

            {ampliada && (
                <VistaAmpliada
                    sessionId={sessionId}
                    vista={ampliada}
                    onCerrar={() => setAmpliada(null)}
                />
            )}
        </>
    );
}