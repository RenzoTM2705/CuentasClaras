package com.cuentasclaras.importer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Detecta la fila de encabezados dentro de una hoja.
 * Inspecciona las primeras filas, cuenta coincidencias con el catálogo de
 * alias y elige la fila con más coincidencias. Devuelve un nivel de confianza.
 */
@Component
public class DetectorEncabezados {

    private static final int MAX_FILAS_INSPECCION = 20;
    private static final int MIN_COINCIDENCIAS_ACEPTABLE = 3;
    private final DataFormatter formatter = new DataFormatter();

    private final CatalogoAlias catalogo;

    public DetectorEncabezados(CatalogoAlias catalogo) {
        this.catalogo = catalogo;
    }

    public ResultadoDetector detectar(Sheet hoja) {
    int ultimaFila = Math.min(hoja.getLastRowNum(), MAX_FILAS_INSPECCION - 1);

    int mejorFila = -1;
    int mejorPuntaje = 0;
    int mejorCeldasNoVacias = 0;
    int mejorTiposDistintos = 0;
    Map<Integer, String> mejorMapeo = new HashMap<>();

    for (int i = 0; i <= ultimaFila; i++) {
        Row fila = hoja.getRow(i);
        if (fila == null) continue;

        Map<Integer, String> mapeo = new HashMap<>();
        int coincidencias = 0;
        int celdasNoVacias = 0;

        for (int c = 0; c < fila.getLastCellNum(); c++) {
            Cell celda = fila.getCell(c);
            if (celda == null)
                continue;
            String valor = formatter.formatCellValue(celda).trim();
            if (valor.isBlank())
                continue;
            celdasNoVacias++;
        

            String campo = catalogo.campoPara(valor);
            if (campo != null) {
                mapeo.put(c, campo);
                coincidencias++;
            }
        }

        // Verifica que las 3 filas siguientes tengan tipos de dato variados
        // (señal de que son datos, no más títulos)
        int tiposDistintos = contarTiposDatos(hoja, i + 1, 3);

        // Puntaje compuesto: coincidencias pesan más, tipos variados confirman
        int puntaje = coincidencias * 10 + tiposDistintos;

        if (coincidencias > 0 && puntaje > mejorPuntaje) {
            mejorPuntaje = puntaje;
            mejorFila = i;
            mejorCeldasNoVacias = celdasNoVacias;
            mejorTiposDistintos = tiposDistintos;
            mejorMapeo = mapeo;
        }
    }

    // Recalcular coincidencias reales del mejor
    int coincidenciasReales = mejorMapeo.size();

    Confianza confianza;
    if (coincidenciasReales >= MIN_COINCIDENCIAS_ACEPTABLE
            && coincidenciasReales >= Math.max(2, mejorCeldasNoVacias / 3)
            && mejorTiposDistintos >= 2) {
        confianza = Confianza.ALTA;
    } else if (coincidenciasReales >= 2) {
        confianza = Confianza.MEDIA;
    } else {
        confianza = Confianza.BAJA;
    }

    return new ResultadoDetector(mejorFila, mejorMapeo, coincidenciasReales, confianza);
}

private int contarTiposDatos(Sheet hoja, int desdeFila, int cuantasFilas) {
    java.util.Set<org.apache.poi.ss.usermodel.CellType> tipos = new java.util.HashSet<>();
    int hasta = Math.min(hoja.getLastRowNum(), desdeFila + cuantasFilas - 1);
    for (int i = desdeFila; i <= hasta; i++) {
        Row f = hoja.getRow(i);
        if (f == null) continue;
        for (int c = 0; c < f.getLastCellNum(); c++) {
            Cell celda = f.getCell(c);
            if (celda != null && celda.getCellType() != org.apache.poi.ss.usermodel.CellType.BLANK) {
                tipos.add(celda.getCellType());
            }
        }
    }
    return tipos.size();
}

    public enum Confianza {
        ALTA, MEDIA, BAJA
    }

    public record ResultadoDetector(
            int filaEncabezado,
            Map<Integer, String> mapeoColumnas,
            int coincidencias,
            Confianza confianza) {
    }
}