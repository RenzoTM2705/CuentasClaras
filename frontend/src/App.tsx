import { useState } from 'react';
import UploadPage from '@/pages/UploadPage';
import ResultadosPage from '@/pages/ResultadosPage';
import type { AnalisisResponse } from '@/types/analisis';

export default function App() {
    const [resultado, setResultado] = useState<AnalisisResponse | null>(null);

    if (resultado) {
        return (
            <ResultadosPage
                resultado={resultado}
                onVolver={() => setResultado(null)}
            />
        );
    }

    return <UploadPage onResultado={setResultado} />;
}