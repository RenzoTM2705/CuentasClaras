package com.cuentasclaras.service;

import com.cuentasclaras.domain.RegistroNormalizado;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CruceProveedoresService {

    public void generarHojaCruce(Workbook wb, List<RegistroNormalizado> registros,
            CellStyle estiloMoneda, CellStyle estiloEncabezado) {
        try (InputStream is = getClass().getResourceAsStream("/plantilla_ti.xlsx")) {
            if (is == null) {
                System.err.println("⚠ No se encontró plantilla_ti.xlsx");
                return;
            }
            Workbook wbPlantilla = WorkbookFactory.create(is);
            Sheet hojaPlantilla = wbPlantilla.getSheetAt(0);
            Sheet hojaCruce = wb.createSheet("Cruce Proveedores");

            Map<String, List<RegistroNormalizado>> cxpPorProveedor = registros.stream()
                    .filter(r -> r.getProveedor() != null)
                    .collect(Collectors.groupingBy(r -> r.getProveedor().toUpperCase().trim()));

            // 1. Definir los encabezados exactos solicitados
            String[] headers = {
                    "Semáforo", "Estado", "Responsable", "Proveedor", "Descripción del servicio",
                    "Tipo servicio", "Nivel criticidad", "Descripción", "Acción a realizar",
                    "Saldo PEN (CxP)", "Saldo USD (CxP)", "N° docs CxP", "Término de pago",
                    "Términos en CxP (n° docs)", "Conciliación", "Observaciones"
            };

            Row filaHeader = hojaCruce.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell c = filaHeader.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(estiloEncabezado);
            }

            // Anchos de columna recomendados para que se vea bien
            hojaCruce.setColumnWidth(0, 1200); // Semáforo
            hojaCruce.setColumnWidth(1, 3000); // Estado
            hojaCruce.setColumnWidth(3, 8000); // Proveedor
            hojaCruce.setColumnWidth(8, 8000); // Acción a realizar
            hojaCruce.setColumnWidth(14, 4000); // Conciliación
            hojaCruce.setColumnWidth(15, 6000); // Observaciones

            int rowNum = 1;
            // Recorremos la plantilla original (asumiendo que las columnas 0 a 6 son las 7
            // originales)
            for (int r = 1; r <= hojaPlantilla.getLastRowNum(); r++) {
                Row rowPl = hojaPlantilla.getRow(r);
                if (rowPl == null)
                    continue;

                // Validar que la fila tenga datos (usando la columna de Proveedor original, que
                // es la 1)
                Cell celdaProveedor = rowPl.getCell(1);
                if (celdaProveedor == null || celdaProveedor.toString().trim().isEmpty()) {
                    continue; // Saltar filas vacías
                }

                Row rowNueva = hojaCruce.createRow(rowNum++);
                String nombreProveedor = celdaProveedor.toString().trim();

                // 2. Copiar datos desde la plantilla a las NUEVAS posiciones
                // Columna 1: Estado (Original col 0)
                if (rowPl.getCell(0) != null)
                    rowNueva.createCell(1).setCellValue(rowPl.getCell(0).toString());
                // Columna 2: Responsable (Original col 0 - ASUMIMOS que la plantilla tiene el
                // color en col 0 y responsable en col 1. Si tu plantilla tiene 7 cols, ajusta
                // estos índices)
                // NOTA: Basado en tu ejemplo, la plantilla original tiene: [Color],
                // [Responsable], [Proveedor], [Desc], [Tipo], [Criticidad], [Accion]
                // Vamos a mapear asumiendo ese orden:
                if (rowPl.getCell(0) != null)
                    rowNueva.createCell(2).setCellValue(rowPl.getCell(0).toString()); // Si el responsable está en col 0

                // Columna 3: Proveedor
                rowNueva.createCell(3).setCellValue(nombreProveedor);

                // Columna 4: Descripción del servicio (Original col 2)
                if (rowPl.getCell(2) != null)
                    rowNueva.createCell(4).setCellValue(rowPl.getCell(2).toString());

                // Columna 5: Tipo servicio (Original col 3)
                if (rowPl.getCell(3) != null)
                    rowNueva.createCell(5).setCellValue(rowPl.getCell(3).toString());

                // Columna 6: Nivel criticidad (Original col 4)
                String criticidadOriginal = rowPl.getCell(4) != null ? rowPl.getCell(4).toString() : "";
                // Columna 7: Descripción (Original col 5)
                if (rowPl.getCell(5) != null)
                    rowNueva.createCell(7).setCellValue(rowPl.getCell(5).toString());
                // Columna 8: Acción a realizar (Original col 6)
                String accion = rowPl.getCell(6) != null ? rowPl.getCell(6).toString().toLowerCase() : "";
                if (rowPl.getCell(6) != null)
                    rowNueva.createCell(8).setCellValue(rowPl.getCell(6).toString());

                // Lógica de Criticidad solicitada: si la acción dice "Pago mensual", es Crítico
                if (accion.contains("pago mensual")) {
                    rowNueva.createCell(6).setCellValue("Crítico");
                } else {
                    rowNueva.createCell(6).setCellValue(criticidadOriginal); // Mantiene la original si no es pago
                                                                             // mensual
                }

                // 3. Lógica de Cruce con CxP
                String provUpper = nombreProveedor.toUpperCase().trim();
                List<RegistroNormalizado> docsCxP = cxpPorProveedor.get(provUpper);

                double saldoPen = 0, saldoUsd = 0;
                int cantDocs = 0;
                Set<String> terminosPago = new HashSet<>();
                // Map para contar cuántos documentos hay por término de pago
                Map<String, Integer> conteoTerminos = new HashMap<>();

                if (docsCxP != null && !docsCxP.isEmpty()) {
                    for (RegistroNormalizado doc : docsCxP) {
                        if ("PEN".equalsIgnoreCase(doc.getMoneda()) && doc.getSaldoMonedaIngresada() != null)
                            saldoPen += doc.getSaldoMonedaIngresada().doubleValue();
                        if ("USD".equalsIgnoreCase(doc.getMoneda()) && doc.getSaldoMonedaIngresada() != null)
                            saldoUsd += doc.getSaldoMonedaIngresada().doubleValue();
                        cantDocs++;

                        // Capturar término de pago (si existe en el objeto RegistroNormalizado)
                        String termino = doc.getTerminoPago() != null ? doc.getTerminoPago().trim() : "Sin información";
                        if (!termino.isEmpty()) {
                            terminosPago.add(termino);
                            conteoTerminos.put(termino, conteoTerminos.getOrDefault(termino, 0) + 1);
                        }
                    }

                    // Escribir Saldos y Docs
                    rowNueva.createCell(9).setCellValue(saldoPen); // Saldo PEN
                    rowNueva.createCell(10).setCellValue(saldoUsd); // Saldo USD
                    rowNueva.createCell(11).setCellValue(cantDocs); // N° docs

                    // Escribir Término de pago (el primero que encuentre o el más común)
                    String terminoPrincipal = terminosPago.isEmpty() ? "Sin información"
                            : terminosPago.iterator().next();
                    rowNueva.createCell(12).setCellValue(terminoPrincipal);

                    // Escribir Términos en CxP (n° docs) ej: "CONTADO (2) | 30 días (1)"
                    if (conteoTerminos.isEmpty()) {
                        rowNueva.createCell(13).setCellValue("Sin información");
                    } else {
                        List<String> partes = new ArrayList<>();
                        for (Map.Entry<String, Integer> entry : conteoTerminos.entrySet()) {
                            partes.add(entry.getKey() + " (" + entry.getValue() + ")");
                        }
                        rowNueva.createCell(13).setCellValue(String.join(" | ", partes));
                    }

                    rowNueva.createCell(14).setCellValue("Con diferencia");
                    rowNueva.createCell(15).setCellValue(""); // Observaciones

                } else {
                    // Si no está en CxP
                    rowNueva.createCell(9).setCellValue(0);
                    rowNueva.createCell(10).setCellValue(0);
                    rowNueva.createCell(11).setCellValue(0);
                    rowNueva.createCell(12).setCellValue("Sin información");
                    rowNueva.createCell(13).setCellValue("");
                    rowNueva.createCell(14).setCellValue("Sin registro en CxP");
                    rowNueva.createCell(15).setCellValue("");
                }

                // 4. SEMÁFORO (Columna 0)
                Cell semaforoNuevo = rowNueva.createCell(0);
                semaforoNuevo.setCellValue("●");

                CellStyle estiloSemaforo = wb.createCellStyle();
                estiloSemaforo.setFillPattern(FillPatternType.NO_FILL);

                XSSFFont fontSemaforo = (XSSFFont) wb.createFont();
                fontSemaforo.setFontName("Arial");
                fontSemaforo.setBold(true);
                fontSemaforo.setFontHeightInPoints((short) 14);

                Cell celdaColorOriginal = rowPl.getCell(0);
                if (celdaColorOriginal != null && celdaColorOriginal.getCellStyle() != null) {
                    if (celdaColorOriginal.getCellStyle().getFillForegroundColorColor() instanceof XSSFColor) {
                        XSSFColor xssfColor = (XSSFColor) celdaColorOriginal.getCellStyle()
                                .getFillForegroundColorColor();
                        fontSemaforo.setColor(xssfColor);
                    } else {
                        fontSemaforo.setColor(celdaColorOriginal.getCellStyle().getFillForegroundColor());
                    }
                } else {
                    fontSemaforo.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
                }

                estiloSemaforo.setFont(fontSemaforo);
                estiloSemaforo.setAlignment(HorizontalAlignment.CENTER);
                semaforoNuevo.setCellStyle(estiloSemaforo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}