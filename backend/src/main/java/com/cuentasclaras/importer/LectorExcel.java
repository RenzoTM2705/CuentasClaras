package com.cuentasclaras.importer;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lee una hoja de Excel a partir de la fila de encabezado detectada.
 * Conserva los valores originales sin transformación.
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
        for (int c = filaEnc.getFirstCellNum(); c < filaEnc.getLastCellNum(); c++) {
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

            for (Map.Entry<Integer, String> e : encabezados.entrySet()) {
                Cell celda = fila.getCell(e.getKey());
                Object valor = valorDeCelda(celda);
                if (valor != null && !(valor instanceof String s && s.isBlank())) {
                    tieneAlgo = true;
                }
                valores.put(e.getValue(), valor);
            }

            if (tieneAlgo) {
                valores.put("__filaOrigen", i + 1);
                filas.add(valores);
            }
        }

        return filas;
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