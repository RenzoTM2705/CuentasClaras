export interface Advertencia {
    codigo: string;
    mensaje: string;
    archivo?: string | null;
    hoja?: string | null;
    fila?: number | null;
}

export interface ResumenMonetario {
    totalPEN: number;
    totalUSD: number;
    totalFuncionalPENConfirmado: number;
    registrosConEquivalenciaPENPendiente: number;
}

export interface ResumenProveedor {
    ruc: string | null;
    proveedor: string | null;
    totalDocumentos: number;
    totalPEN: number;
    totalUSD: number;
    registrosConEquivalenciaPENPendiente: number;
}

export interface VistaGenerada {
    id: string;
    nombre: string;
    tipo: 'archivo' | 'unidad' | 'combinado';
    totalDocumentos: number;
    resumen: ResumenMonetario;
    resumenPorProveedor: ResumenProveedor[];
    unidadesOperativas: string[];
    columnas: string[];
    filasPreview: (string | number | null)[][];
}

export interface VistaCompleta {
    id: string;
    nombre: string;
    tipo: 'archivo' | 'unidad' | 'combinado';
    totalDocumentos: number;
    resumen: ResumenMonetario;
    resumenPorProveedor: ResumenProveedor[];
    unidadesOperativas: string[];
    columnas: string[];
    filas: (string | number | null)[][];
}

export interface ResumenCombinacion {
    totalArchivos: number;
    totalRegistros: number;
    registrosUnicos: number;
    duplicadosExactos: number;
    duplicadosParciales: number;
    advertenciasEstructurales: string[];
}

export interface AnalisisResponse {
    sessionId: string;
    totalFilasLeidas: number;
    totalRegistrosValidos: number;
    totalRegistrosRechazados: number;
    vistas: VistaGenerada[];
    advertencias: Advertencia[];
    resumenCombinacion: ResumenCombinacion;
}

export type ModoAnalisis = 'combinar' | 'independiente' | 'ambos';