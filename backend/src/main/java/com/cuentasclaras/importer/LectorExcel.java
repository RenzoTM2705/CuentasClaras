package com.cuentasclaras.importer;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Lee una hoja de Excel a partir de la fila de encabezado detectada.
 * Conserva los valores originales sin transformación.
 *
 * <p>
 * Las filas completamente vacías o con todos los valores "null"/"NULL"
 * se descartan silenciosamente: no cuentan como registros ni generan
 * advertencias.
 * </p>
 */
@Component
public class LectorExcel {

    private final DataFormatter formatter = new DataFormatter();
    private FormulaEvaluator evaluator;

    public LectorExcel() {
    }

    public void setWorkbook(Workbook workbook) {
        this.evaluator = workbook.getCreationHelper().createFormulaEvaluator();
    }

    public List<Map<String, Object>> leerDesde(Sheet hoja, int filaEncabezado) {
        Row filaEnc = hoja.getRow(filaEncabezado);
        if (filaEnc == null)
            return List.of();

        Map<Integer, String> encabezados = new LinkedHashMap<>();
        for (int c = 0; c < filaEnc.getLastCellNum(); c++) {
            Cell celda = filaEnc.getCell(c);
            String valor = celda == null ? "" : formatter.formatCellValue(celda).trim();
            if (!valor.isBlank()) {
                encabezados.put(c, valor);
            }
        }

        List<Map<String, Object>> filas = new ArrayList<>();
        int ultimaFila = hoja.getLastRowNum();

        for (int i = filaEncabezado + 1; i <= ultimaFila; i++) {
            Row fila = hoja.getRow(i);
            if (fila == null)
                continue;

            Map<String, Object> valores = new LinkedHashMap<>();
            boolean tieneAlgo = false;
            boolean tieneContenidoReal = false;

            for (Map.Entry<Integer, String> e : encabezados.entrySet()) {
                Cell celda = fila.getCell(e.getKey());
                Object valor = valorDeCelda(celda);
                valores.put(e.getValue(), valor);

                if (valor != null && !(valor instanceof String s && s.isBlank())) {
                    tieneAlgo = true;
                    // ¿Es contenido real o es un literal "null"/"NULL"?
                    if (!esLiteralNull(valor)) {
                        tieneContenidoReal = true;
                    }
                }
            }

            // Descartar filas vacías o con todos los valores "null"/"NULL"
            if (!tieneAlgo || !tieneContenidoReal) {
                continue;
            }

            valores.put("__filaOrigen", i + 1);
            filas.add(valores);
        }

        return filas;
    }

    /**
     * Detecta si un valor es un literal de nulo exportado por otros sistemas
     * (NULL, null, #N/A, N/A, "-").
     */
    private boolean esLiteralNull(Object valor) {
        if (valor == null)
            return true;
        if (!(valor instanceof String s))
            return false;
        String n = s.trim().toLowerCase(Locale.ROOT);
        return n.equals("null") || n.equals("#n/a") || n.equals("n/a") || n.equals("-");
    }

    private Object valorDeCelda(Cell celda) {
        if (celda == null)
            return null;
        return switch (celda.getCellType()) {
            case STRING -> celda.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(celda)
                    ? celda.getLocalDateTimeCellValue().toLocalDate()
                    : celda.getNumericCellValue();
            case BOOLEAN -> celda.getBooleanCellValue();
            case FORMULA -> {
                if (evaluator == null)
                    yield formatter.formatCellValue(celda);
                CellValue v = evaluator.evaluate(celda);
                if (v == null)
                    yield null;
                yield switch (v.getCellType()) {
                    case STRING -> v.getStringValue();
                    case NUMERIC -> DateUtil.isCellDateFormatted(celda)
                            ? celda.getLocalDateTimeCellValue().toLocalDate()
                            : v.getNumberValue();
                    case BOOLEAN -> v.getBooleanValue();
                    default -> null;
                };
            }
            default -> null;
        };
    }
}