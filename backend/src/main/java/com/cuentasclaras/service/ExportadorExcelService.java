package com.cuentasclaras.service;

import com.cuentasclaras.domain.ResumenMonetario;
import com.cuentasclaras.domain.ResumenProveedor;
import com.cuentasclaras.domain.VistaCompleta;
import com.cuentasclaras.dto.ExportarRequest;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Genera un archivo .xlsx con la vista completa o filtrada.
 *
 * <p>
 * Dos hojas:
 * </p>
 * <ol>
 * <li><b>Detalle</b>: encabezados originales + todas las filas.</li>
 * <li><b>Resumen</b>: totales generales, totales por unidad operativa y
 * desglose por proveedor.</li>
 * </ol>
 *
 * <p>
 * PEN y USD nunca se suman entre sí.
 * </p>
 */
@Service
public class ExportadorExcelService {

    /** Palabras que identifican columnas monetarias. */
    private static final String[] CLAVES_MONEDA = {
            "saldo", "importe", "monto", "total", "subtotal", "igv", "detraccion",
            "tipo de cambio", "tipo cambio", "tc"
    };

    /** Palabras que identifican columnas de fecha. */
    private static final String[] CLAVES_FECHA = {
            "fecha", "vencimiento", "emision", "contable", "recepcion", "corte"
    };

    /** Palabras que identifican columnas de identificadores (texto). */
    private static final String[] CLAVES_TEXTO = {
            "ruc", "voucher", "numero", "nro", "cuenta", "oc", "telefono",
            "codigo", "factura", "comprobante", "documento"
    };

    public byte[] exportar(VistaCompleta vista, ExportarRequest filtros) {
        try (Workbook wb = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle estiloEncabezado = crearEstiloEncabezado(wb);
            CellStyle estiloMoneda = crearEstiloMoneda(wb);
            CellStyle estiloFecha = crearEstiloFecha(wb);
            CellStyle estiloTitulo = crearEstiloTitulo(wb);
            CellStyle estiloTexto = crearEstiloTexto(wb);
            CellStyle estiloSubtitulo = crearEstiloSubtitulo(wb);

            List<List<Object>> filasFiltradas = filtrarFilas(vista, filtros);

            crearHojaDetalle(wb, vista, filasFiltradas, estiloEncabezado, estiloMoneda, estiloFecha, estiloTexto);
            crearHojaResumen(wb, vista, filasFiltradas, estiloTitulo, estiloSubtitulo, estiloEncabezado, estiloMoneda);

            wb.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el Excel: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------ Detalle

    private void crearHojaDetalle(Workbook wb,
            VistaCompleta vista,
            List<List<Object>> filas,
            CellStyle estiloEncabezado,
            CellStyle estiloMoneda,
            CellStyle estiloFecha,
            CellStyle estiloTexto) {
        Sheet hoja = wb.createSheet("Detalle");

        Row enc = hoja.createRow(0);
        for (int i = 0; i < vista.getColumnas().size(); i++) {
            Cell c = enc.createCell(i);
            c.setCellValue(vista.getColumnas().get(i));
            c.setCellStyle(estiloEncabezado);
        }

        int numCols = vista.getColumnas().size();
        TipoColumna[] tipos = new TipoColumna[numCols];
        for (int i = 0; i < numCols; i++) {
            tipos[i] = clasificarColumna(vista.getColumnas().get(i));
        }

        for (int r = 0; r < filas.size(); r++) {
            Row row = hoja.createRow(r + 1);
            List<Object> fila = filas.get(r);
            for (int c = 0; c < fila.size() && c < numCols; c++) {
                Cell cell = row.createCell(c);
                escribirCelda(cell, fila.get(c), tipos[c], estiloMoneda, estiloFecha, estiloTexto);
            }
        }

        for (int i = 0; i < numCols; i++) {
            hoja.setColumnWidth(i, 4000);
        }
    }

    private void escribirCelda(Cell cell, Object valor, TipoColumna tipo,
            CellStyle estiloMoneda, CellStyle estiloFecha, CellStyle estiloTexto) {
        if (valor == null)
            return;

        switch (tipo) {
            case FECHA: {
                LocalDate fecha = comoFecha(valor);
                if (fecha != null) {
                    cell.setCellValue(fecha);
                    cell.setCellStyle(estiloFecha);
                    return;
                }
                cell.setCellValue(valor.toString());
                cell.setCellStyle(estiloTexto);
                return;
            }
            case MONEDA: {
                BigDecimal num = comoNumero(valor);
                if (num != null) {
                    cell.setCellValue(num.doubleValue());
                    cell.setCellStyle(estiloMoneda);
                    return;
                }
                cell.setCellValue(valor.toString());
                cell.setCellStyle(estiloTexto);
                return;
            }
            case TEXTO:
            default: {
                cell.setCellValue(valor.toString());
                cell.setCellStyle(estiloTexto);
                return;
            }
        }
    }

    private LocalDate comoFecha(Object v) {
        if (v == null)
            return null;
        if (v instanceof LocalDate ld)
            return ld;
        if (v instanceof java.util.Date d) {
            return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        }
        String s = v.toString().trim();
        if (s.length() < 8 || s.length() > 10)
            return null;
        try {
            if (s.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
                String[] p = s.split("/");
                return LocalDate.of(Integer.parseInt(p[2]), Integer.parseInt(p[1]), Integer.parseInt(p[0]));
            }
            if (s.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDate.parse(s);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private BigDecimal comoNumero(Object v) {
        if (v == null)
            return null;
        if (v instanceof BigDecimal b)
            return b;
        if (v instanceof Number n)
            return BigDecimal.valueOf(n.doubleValue());
        try {
            String s = v.toString().replace(",", "").trim();
            if (s.isEmpty())
                return null;
            return new BigDecimal(s);
        } catch (Exception e) {
            return null;
        }
    }

    // ------------------------------------------------------------------
    // Clasificación

    private enum TipoColumna {
        MONEDA, FECHA, TEXTO
    }

    private TipoColumna clasificarColumna(String encabezado) {
        if (encabezado == null)
            return TipoColumna.TEXTO;
        String n = normalizar(encabezado);

        for (String clave : CLAVES_TEXTO) {
            if (n.contains(clave)) {
                for (String mon : CLAVES_MONEDA) {
                    if (n.contains(mon)) {
                        String primera = n.split(" ")[0];
                        if (esClaveTexto(primera))
                            return TipoColumna.TEXTO;
                        return TipoColumna.MONEDA;
                    }
                }
                return TipoColumna.TEXTO;
            }
        }

        for (String clave : CLAVES_FECHA) {
            if (n.contains(clave))
                return TipoColumna.FECHA;
        }

        for (String clave : CLAVES_MONEDA) {
            if (n.contains(clave))
                return TipoColumna.MONEDA;
        }

        return TipoColumna.TEXTO;
    }

    private boolean esClaveTexto(String palabra) {
        for (String t : CLAVES_TEXTO) {
            if (palabra.equals(t))
                return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ Resumen

    private void crearHojaResumen(Workbook wb,
            VistaCompleta vista,
            List<List<Object>> filasFiltradas,
            CellStyle estiloTitulo,
            CellStyle estiloSubtitulo,
            CellStyle estiloEncabezado,
            CellStyle estiloMoneda) {
        Sheet hoja = wb.createSheet("Resumen");

        int r = 0;

        // --- Título principal
        Row t = hoja.createRow(r++);
        Cell ct = t.createCell(0);
        ct.setCellValue("Resumen financiero");
        ct.setCellStyle(estiloTitulo);

        // --- Totales generales
        ResumenMonetario resumen = calcularResumen(vista, filasFiltradas);

        r = filaTotales(hoja, r, "Total PEN", resumen.getTotalPEN().doubleValue(), estiloEncabezado, estiloMoneda);
        r = filaTotales(hoja, r, "Total USD", resumen.getTotalUSD().doubleValue(), estiloEncabezado, estiloMoneda);

        r += 2;

        // --- Bloque: totales por unidad operativa
        Row tituloUnidades = hoja.createRow(r++);
        Cell ctu = tituloUnidades.createCell(0);
        ctu.setCellValue("Total de deuda por unidad operativa");
        ctu.setCellStyle(estiloSubtitulo);

        Row encUnidades = hoja.createRow(r++);
        String[] colsUnidades = { "Unidad operativa", "Total PEN", "Total USD" };
        for (int i = 0; i < colsUnidades.length; i++) {
            Cell c = encUnidades.createCell(i);
            c.setCellValue(colsUnidades[i]);
            c.setCellStyle(estiloEncabezado);
        }

        List<ResumenUnidad> unidades = calcularResumenPorUnidad(vista, filasFiltradas);
        for (ResumenUnidad ru : unidades) {
            Row row = hoja.createRow(r++);
            row.createCell(0).setCellValue(ru.unidad == null ? "(sin unidad)" : ru.unidad);

            Cell cPen = row.createCell(1);
            cPen.setCellValue(ru.totalPEN.doubleValue());
            cPen.setCellStyle(estiloMoneda);

            Cell cUsd = row.createCell(2);
            cUsd.setCellValue(ru.totalUSD.doubleValue());
            cUsd.setCellStyle(estiloMoneda);
        }

        r += 2;

        // --- Bloque: totales por proveedor (sin columna Docs)
        Row tituloProv = hoja.createRow(r++);
        Cell ctp = tituloProv.createCell(0);
        ctp.setCellValue("Total de deuda por proveedor");
        ctp.setCellStyle(estiloSubtitulo);

        Row encProv = hoja.createRow(r++);
        String[] colsProv = { "RUC", "Proveedor", "Total PEN", "Total USD" };
        for (int i = 0; i < colsProv.length; i++) {
            Cell c = encProv.createCell(i);
            c.setCellValue(colsProv[i]);
            c.setCellStyle(estiloEncabezado);
        }

        List<ResumenProveedor> proveedores = calcularResumenPorProveedor(vista, filasFiltradas);
        for (ResumenProveedor rp : proveedores) {
            Row row = hoja.createRow(r++);
            row.createCell(0).setCellValue(rp.getRuc() == null ? "" : rp.getRuc());
            row.createCell(1).setCellValue(rp.getProveedor() == null ? "" : rp.getProveedor());

            Cell cPen = row.createCell(2);
            cPen.setCellValue(rp.getTotalPEN().doubleValue());
            cPen.setCellStyle(estiloMoneda);

            Cell cUsd = row.createCell(3);
            cUsd.setCellValue(rp.getTotalUSD().doubleValue());
            cUsd.setCellStyle(estiloMoneda);
        }

        // Anchos de columna
        for (int i = 0; i < 5; i++) {
            hoja.setColumnWidth(i, 5000);
        }
    }

    private int filaTotales(Sheet hoja, int r, String etiqueta, double valor,
            CellStyle estiloEncabezado, CellStyle estiloMoneda) {
        Row row = hoja.createRow(r);
        Cell c1 = row.createCell(0);
        c1.setCellValue(etiqueta);
        c1.setCellStyle(estiloEncabezado);

        Cell c2 = row.createCell(1);
        c2.setCellValue(valor);
        c2.setCellStyle(estiloMoneda);

        return r + 1;
    }

    // ------------------------------------------------------------------ Cálculos

    private ResumenMonetario calcularResumen(VistaCompleta vista, List<List<Object>> filas) {
        int idxMoneda = indiceColumna(vista, new String[] { "divisa", "moneda", "currency" });
        int idxSaldoIngresado = indiceColumna(vista,
                new String[] { "saldo en moneda ingresada", "saldo moneda ingresada" });
        int idxSaldoUSD = indiceColumna(vista, new String[] { "saldo usd", "saldo en dolares", "saldo dolares" });
        int idxSaldoPEN = indiceColumna(vista, new String[] { "saldo pen", "saldo soles", "saldo en soles" });

        BigDecimal totalPEN = BigDecimal.ZERO;
        BigDecimal totalUSD = BigDecimal.ZERO;

        for (List<Object> fila : filas) {
            String moneda = idxMoneda >= 0 && fila.get(idxMoneda) != null
                    ? fila.get(idxMoneda).toString().toUpperCase(Locale.ROOT)
                    : "";
            BigDecimal saldoIngresado = idxSaldoIngresado >= 0 ? aBigDecimal(fila.get(idxSaldoIngresado)) : null;
            BigDecimal saldoUSD = idxSaldoUSD >= 0 ? aBigDecimal(fila.get(idxSaldoUSD)) : null;
            BigDecimal saldoPEN = idxSaldoPEN >= 0 ? aBigDecimal(fila.get(idxSaldoPEN)) : null;

            if ("PEN".equals(moneda)) {
                BigDecimal v = saldoPEN != null ? saldoPEN : saldoIngresado;
                if (v != null)
                    totalPEN = totalPEN.add(v);
            } else if ("USD".equals(moneda)) {
                BigDecimal v = saldoUSD != null ? saldoUSD : saldoIngresado;
                if (v != null)
                    totalUSD = totalUSD.add(v);
            }
        }

        ResumenMonetario r = new ResumenMonetario();
        r.setTotalPEN(totalPEN);
        r.setTotalUSD(totalUSD);
        return r;
    }

    /**
     * Calcula el total por unidad operativa. La unidad se detecta por la
     * columna cuyo encabezado normalizado sea "unidad operativa", "unidad" o "uo".
     */
    private List<ResumenUnidad> calcularResumenPorUnidad(VistaCompleta vista, List<List<Object>> filas) {
        int idxUnidad = indiceColumna(vista, new String[] { "unidad operativa", "unidad", "uo" });
        int idxMoneda = indiceColumna(vista, new String[] { "divisa", "moneda", "currency" });
        int idxSaldoIngresado = indiceColumna(vista,
                new String[] { "saldo en moneda ingresada", "saldo moneda ingresada" });
        int idxSaldoUSD = indiceColumna(vista, new String[] { "saldo usd", "saldo en dolares", "saldo dolares" });
        int idxSaldoPEN = indiceColumna(vista, new String[] { "saldo pen", "saldo soles", "saldo en soles" });

        Map<String, ResumenUnidad> mapa = new LinkedHashMap<>();

        for (List<Object> fila : filas) {
            String unidad = idxUnidad >= 0 && fila.get(idxUnidad) != null
                    ? fila.get(idxUnidad).toString().trim()
                    : "(sin unidad)";

            ResumenUnidad ru = mapa.computeIfAbsent(unidad, u -> new ResumenUnidad(u));

            String moneda = idxMoneda >= 0 && fila.get(idxMoneda) != null
                    ? fila.get(idxMoneda).toString().toUpperCase(Locale.ROOT)
                    : "";
            BigDecimal saldoIngresado = idxSaldoIngresado >= 0 ? aBigDecimal(fila.get(idxSaldoIngresado)) : null;
            BigDecimal saldoUSD = idxSaldoUSD >= 0 ? aBigDecimal(fila.get(idxSaldoUSD)) : null;
            BigDecimal saldoPEN = idxSaldoPEN >= 0 ? aBigDecimal(fila.get(idxSaldoPEN)) : null;

            if ("PEN".equals(moneda)) {
                BigDecimal v = saldoPEN != null ? saldoPEN : saldoIngresado;
                if (v != null)
                    ru.totalPEN = ru.totalPEN.add(v);
            } else if ("USD".equals(moneda)) {
                BigDecimal v = saldoUSD != null ? saldoUSD : saldoIngresado;
                if (v != null)
                    ru.totalUSD = ru.totalUSD.add(v);
            }
        }

        List<ResumenUnidad> lista = new ArrayList<>(mapa.values());
        lista.sort((a, b) -> b.totalPEN.compareTo(a.totalPEN));
        return lista;
    }

    private List<ResumenProveedor> calcularResumenPorProveedor(VistaCompleta vista, List<List<Object>> filas) {
        int idxRuc = indiceColumna(vista, new String[] { "ruc" });
        int idxProveedor = indiceColumna(vista, new String[] { "proveedor" });
        int idxMoneda = indiceColumna(vista, new String[] { "divisa", "moneda", "currency" });
        int idxSaldoIngresado = indiceColumna(vista,
                new String[] { "saldo en moneda ingresada", "saldo moneda ingresada" });
        int idxSaldoUSD = indiceColumna(vista, new String[] { "saldo usd", "saldo en dolares", "saldo dolares" });
        int idxSaldoPEN = indiceColumna(vista, new String[] { "saldo pen", "saldo soles", "saldo en soles" });

        Map<String, ResumenProveedor> mapa = new LinkedHashMap<>();

        for (List<Object> fila : filas) {
            String ruc = idxRuc >= 0 && fila.get(idxRuc) != null ? fila.get(idxRuc).toString().trim() : "";
            String prov = idxProveedor >= 0 && fila.get(idxProveedor) != null ? fila.get(idxProveedor).toString().trim()
                    : "";

            String clave = !ruc.isEmpty() ? "RUC:" + ruc
                    : (!prov.isEmpty() ? "NOMBRE:" + prov.toUpperCase(Locale.ROOT) : "SIN");

            ResumenProveedor rp = mapa.computeIfAbsent(clave,
                    k -> new ResumenProveedor(ruc, prov.isEmpty() ? "(sin proveedor)" : prov));

            String moneda = idxMoneda >= 0 && fila.get(idxMoneda) != null
                    ? fila.get(idxMoneda).toString().toUpperCase(Locale.ROOT)
                    : "";
            BigDecimal saldoIngresado = idxSaldoIngresado >= 0 ? aBigDecimal(fila.get(idxSaldoIngresado)) : null;
            BigDecimal saldoUSD = idxSaldoUSD >= 0 ? aBigDecimal(fila.get(idxSaldoUSD)) : null;
            BigDecimal saldoPEN = idxSaldoPEN >= 0 ? aBigDecimal(fila.get(idxSaldoPEN)) : null;

            if ("PEN".equals(moneda)) {
                BigDecimal v = saldoPEN != null ? saldoPEN : saldoIngresado;
                if (v != null)
                    rp.setTotalPEN(rp.getTotalPEN().add(v));
            } else if ("USD".equals(moneda)) {
                BigDecimal v = saldoUSD != null ? saldoUSD : saldoIngresado;
                if (v != null)
                    rp.setTotalUSD(rp.getTotalUSD().add(v));
            }
        }

        List<ResumenProveedor> lista = new ArrayList<>(mapa.values());
        lista.sort((a, b) -> {
            int cmp = b.getTotalPEN().compareTo(a.getTotalPEN());
            if (cmp != 0)
                return cmp;
            return b.getTotalUSD().compareTo(a.getTotalUSD());
        });
        return lista;
    }

    private int indiceColumna(VistaCompleta vista, String[] candidatos) {
        for (int i = 0; i < vista.getColumnas().size(); i++) {
            String n = normalizar(vista.getColumnas().get(i));
            for (String c : candidatos) {
                if (n.equals(c))
                    return i;
            }
        }
        return -1;
    }

    private String normalizar(String s) {
        if (s == null)
            return "";
        return s.toLowerCase(Locale.ROOT)
                .replaceAll("[áàäâ]", "a")
                .replaceAll("[éèëê]", "e")
                .replaceAll("[íìïî]", "i")
                .replaceAll("[óòöô]", "o")
                .replaceAll("[úùüû]", "u")
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }

    private BigDecimal aBigDecimal(Object v) {
        if (v == null)
            return null;
        if (v instanceof BigDecimal b)
            return b;
        if (v instanceof Number n)
            return BigDecimal.valueOf(n.doubleValue());
        try {
            String s = v.toString().replace(",", "").trim();
            if (s.isEmpty())
                return null;
            return new BigDecimal(s);
        } catch (Exception e) {
            return null;
        }
    }

    // ------------------------------------------------------------------ Filtros

    private List<List<Object>> filtrarFilas(VistaCompleta vista, ExportarRequest filtros) {
        if (filtros == null)
            return vista.getFilas();

        int idxProveedor = indiceColumna(vista, new String[] { "proveedor" });
        int idxRuc = indiceColumna(vista, new String[] { "ruc" });
        int idxUnidad = indiceColumna(vista, new String[] { "unidad operativa", "unidad", "uo" });
        int idxMoneda = indiceColumna(vista, new String[] { "divisa", "moneda", "currency" });

        List<List<Object>> resultado = new ArrayList<>();

        for (List<Object> fila : vista.getFilas()) {
            if (filtros.getProveedor() != null && !filtros.getProveedor().isBlank()) {
                String q = filtros.getProveedor().toLowerCase(Locale.ROOT).trim();
                String prov = idxProveedor >= 0 && fila.get(idxProveedor) != null
                        ? fila.get(idxProveedor).toString().toLowerCase(Locale.ROOT)
                        : "";
                String ruc = idxRuc >= 0 && fila.get(idxRuc) != null
                        ? fila.get(idxRuc).toString().toLowerCase(Locale.ROOT)
                        : "";
                if (!prov.contains(q) && !ruc.contains(q))
                    continue;
            }

            if (filtros.getUnidades() != null && !filtros.getUnidades().isEmpty()) {
                String u = idxUnidad >= 0 && fila.get(idxUnidad) != null ? fila.get(idxUnidad).toString() : "";
                if (!filtros.getUnidades().contains(u))
                    continue;
            }

            if (filtros.getMonedas() != null && !filtros.getMonedas().isEmpty()) {
                String m = idxMoneda >= 0 && fila.get(idxMoneda) != null
                        ? fila.get(idxMoneda).toString().toUpperCase(Locale.ROOT)
                        : "";
                if (!filtros.getMonedas().contains(m))
                    continue;
            }

            resultado.add(fila);
        }

        return resultado;
    }

    // ------------------------------------------------------------------ Estilos

    private CellStyle crearEstiloEncabezado(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setBorderBottom(BorderStyle.THIN);
        return s;
    }

    private CellStyle crearEstiloMoneda(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        DataFormat df = wb.createDataFormat();
        s.setDataFormat(df.getFormat("#,##0.00"));
        return s;
    }

    private CellStyle crearEstiloFecha(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        DataFormat df = wb.createDataFormat();
        s.setDataFormat(df.getFormat("dd/mm/yyyy"));
        return s;
    }

    private CellStyle crearEstiloTitulo(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 14);
        s.setFont(f);
        return s;
    }

    private CellStyle crearEstiloSubtitulo(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 12);
        s.setFont(f);
        return s;
    }

    private CellStyle crearEstiloTexto(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        DataFormat df = wb.createDataFormat();
        s.setDataFormat(df.getFormat("@"));
        return s;
    }

    // ------------------------------------------------------------------ Auxiliares

    /**
     * Estructura interna para acumular totales por unidad operativa.
     */
    private static class ResumenUnidad {
        final String unidad;
        BigDecimal totalPEN = BigDecimal.ZERO;
        BigDecimal totalUSD = BigDecimal.ZERO;

        ResumenUnidad(String unidad) {
            this.unidad = unidad;
        }
    }
}