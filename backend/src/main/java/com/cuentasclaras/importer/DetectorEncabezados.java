package com.cuentasclaras.importer;

import org.apache.poi.ss.usermodel.Cell;
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

    private final CatalogoAlias catalogo;

    public DetectorEncabezados(CatalogoAlias catalogo) {
        this.catalogo = catalogo;
    }

    public ResultadoDetector detectar(Sheet hoja) {
        int ultimaFila = Math.min(hoja.getLastRowNum(), MAX_FILAS_INSPECCION - 1);

        int mejorFila = -1;
        int mejorPuntaje = 0;
        int mejorCeldasNoVacias = 0;
        Map<Integer, String> mejorMapeo = new HashMap<>();

        for (int i = 0; i <= ultimaFila; i++) {
            Row fila = hoja.getRow(i);
            if (fila == null)
                continue;

            Map<Integer, String> mapeo = new HashMap<>();
            int coincidencias = 0;
            int celdasNoVacias = 0;

            for (int c = fila.getFirstCellNum(); c < fila.getLastCellNum(); c++) {
                Cell celda = fila.getCell(c);
                if (celda == null)
                    continue;
                String valor = celda.getStringCellValue();
                if (valor == null || valor.isBlank())
                    continue;
                celdasNoVacias++;

                String campo = catalogo.campoPara(valor);
                if (campo != null) {
                    mapeo.put(c, campo);
                    coincidencias++;
                }
            }

            if (coincidencias > mejorPuntaje) {
                mejorPuntaje = coincidencias;
                mejorFila = i;
                mejorCeldasNoVacias = celdasNoVacias;
                mejorMapeo = mapeo;
            }
        }

        Confianza confianza;
        if (mejorPuntaje >= MIN_COINCIDENCIAS_ACEPTABLE
                && mejorPuntaje >= Math.max(2, mejorCeldasNoVacias / 3)) {
            confianza = Confianza.ALTA;
        } else if (mejorPuntaje >= 2) {
            confianza = Confianza.MEDIA;
        } else {
            confianza = Confianza.BAJA;
        }

        return new ResultadoDetector(mejorFila, mejorMapeo, mejorPuntaje, confianza);
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