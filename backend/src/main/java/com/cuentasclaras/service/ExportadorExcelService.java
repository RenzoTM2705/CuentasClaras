package com.cuentasclaras.service;

import com.cuentasclaras.domain.RegistroNormalizado;
import com.cuentasclaras.domain.ResumenMonetario;
import com.cuentasclaras.domain.ResumenProveedor;
import com.cuentasclaras.domain.VistaCompleta;
import com.cuentasclaras.dto.ExportarRequest;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Genera un archivo .xlsx con la vista completa o filtrada.
 */
@Service
public class ExportadorExcelService {

    private final CruceProveedoresService cruceService;

    public ExportadorExcelService(CruceProveedoresService cruceService) {
        this.cruceService = cruceService;
    }

    private static final String[] CLAVES_MONEDA = {
            "saldo", "importe", "monto", "total", "subtotal", "igv", "detraccion",
            "tipo de cambio", "tipo cambio", "tc"
    };

    private static final String[] CLAVES_FECHA = {
            "fecha", "vencimiento", "emision", "contable", "recepcion", "corte"
    };

    private static final String[] CLAVES_TEXTO = {
            "ruc", "voucher", "numero", "nro", "cuenta", "oc", "telefono",
            "codigo", "factura", "comprobante", "documento"
    };

    public byte[] exportar(VistaCompleta vista, ExportarRequest filtros, List<RegistroNormalizado> todosLosRegistros) {
        try (Workbook wb = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle estiloEncabezado = crearEstiloEncabezado(wb);
            CellStyle estiloMoneda = crearEstiloMoneda(wb);
            CellStyle estiloFecha = crearEstiloFecha(wb);
            CellStyle estiloTitulo = crearEstiloTitulo(wb);
            CellStyle estiloTexto = crearEstiloTexto(wb);
            CellStyle estiloSubtitulo = crearEstiloSubtitulo(wb);
            CellStyle estiloTotalFila = crearEstiloTotalFila(wb);

            // 1. Generar la lista de List<Object> para las hojas existentes
            List<List<Object>> filasFiltradas = new ArrayList<>();
            for (RegistroNormalizado r : todosLosRegistros) {
                if (r.getValoresOriginales() == null)
                    continue;
                List<Object> fila = new ArrayList<>();
                for (Map.Entry<String, Object> e : r.getValoresOriginales().entrySet()) {
                    if (!"__filaOrigen".equals(e.getKey()))
                        fila.add(e.getValue());
                }
                filasFiltradas.add(fila);
            }

            // 2. Crear hojas originales
            crearHojaDetalle(wb, vista, filasFiltradas, estiloEncabezado, estiloMoneda, estiloFecha, estiloTexto);
            crearHojaResumen(wb, vista, filasFiltradas, estiloTitulo, estiloSubtitulo, estiloEncabezado, estiloMoneda);
            crearHojaTratado(wb, vista, filasFiltradas, estiloEncabezado, estiloMoneda, estiloTexto, estiloTotalFila);

            // 3. Añadir la nueva lógica
            agregarComparativaFechas(wb, todosLosRegistros, estiloSubtitulo, estiloEncabezado, estiloMoneda);
            cruceService.generarHojaCruce(wb, todosLosRegistros, estiloMoneda, estiloEncabezado);

            wb.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el Excel: " + e.getMessage(), e);
        }
    }

    // --- NUEVO MÉTODO PARA LA COMPARATIVA ---
    private void agregarComparativaFechas(Workbook wb, List<RegistroNormalizado> registros,
            CellStyle estiloSubtitulo, CellStyle estiloEncabezado, CellStyle estiloMoneda) {
        Sheet hoja = wb.getSheet("Resumen");
        if (hoja == null)
            return;

        Set<String> fechas = registros.stream()
                .map(RegistroNormalizado::getFechaReporte)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));

        if (fechas.size() > 1) {
            int r = hoja.getLastRowNum() + 3;
            Row tituloComp = hoja.createRow(r++);
            Cell ctc = tituloComp.createCell(0);
            ctc.setCellValue("Comparativa por fechas");
            ctc.setCellStyle(estiloSubtitulo);

            Row encComp = hoja.createRow(r++);
            encComp.createCell(0).setCellValue("Proveedor");
            int colIdx = 1;
            List<String> fechasList = new ArrayList<>(fechas);
            for (String f : fechasList) {
                Cell c = encComp.createCell(colIdx++);
                c.setCellValue(f);
                c.setCellStyle(estiloEncabezado);
            }
            encComp.createCell(colIdx).setCellValue("Diferencia");
            encComp.getCell(colIdx).setCellStyle(estiloEncabezado);

            Map<String, Map<String, Double>> compPen = new LinkedHashMap<>();
            Map<String, Map<String, Double>> compUsd = new LinkedHashMap<>();

            for (RegistroNormalizado reg : registros) {
                String prov = reg.getProveedor() != null ? reg.getProveedor() : "(sin proveedor)";
                String fecha = reg.getFechaReporte() != null ? reg.getFechaReporte() : "Desconocida";
                double saldo = reg.getSaldoMonedaIngresada() != null ? reg.getSaldoMonedaIngresada().doubleValue() : 0;
                boolean isPen = "PEN".equalsIgnoreCase(reg.getMoneda());

                if (isPen)
                    compPen.computeIfAbsent(prov, k -> new HashMap<>()).merge(fecha, saldo, Double::sum);
                else
                    compUsd.computeIfAbsent(prov, k -> new HashMap<>()).merge(fecha, saldo, Double::sum);
            }

            for (Map.Entry<String, Map<String, Double>> entry : compPen.entrySet()) {
                Row row = hoja.createRow(r++);
                row.createCell(0).setCellValue(entry.getKey() + " (PEN)");
                int c = 1;
                double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
                for (String f : fechasList) {
                    Double val = entry.getValue().getOrDefault(f, 0.0);
                    Cell cell = row.createCell(c++);
                    cell.setCellValue(val);
                    cell.setCellStyle(estiloMoneda);
                    if (val < min)
                        min = val;
                    if (val > max)
                        max = val;
                }
                Cell cDif = row.createCell(c);
                cDif.setCellValue(max - min);
                cDif.setCellStyle(estiloMoneda);
            }

            for (Map.Entry<String, Map<String, Double>> entry : compUsd.entrySet()) {
                Row row = hoja.createRow(r++);
                row.createCell(0).setCellValue(entry.getKey() + " (USD)");
                int c = 1;
                double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
                for (String f : fechasList) {
                    Double val = entry.getValue().getOrDefault(f, 0.0);
                    Cell cell = row.createCell(c++);
                    cell.setCellValue(val);
                    cell.setCellStyle(estiloMoneda);
                    if (val < min)
                        min = val;
                    if (val > max)
                        max = val;
                }
                Cell cDif = row.createCell(c);
                cDif.setCellValue(max - min);
                cDif.setCellStyle(estiloMoneda);
            }
        }
    }

    // ------------------------------------------------------------------ Detalle
    // (Se mantiene exactamente igual que en tu archivo original)
    private void crearHojaDetalle(Workbook wb, VistaCompleta vista, List<List<Object>> filas,
            CellStyle estiloEncabezado, CellStyle estiloMoneda, CellStyle estiloFecha, CellStyle estiloTexto) {
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

        int filasEscritas = 0;
        for (List<Object> fila : filas) {
            if (esFilaNull(fila))
                continue;
            Row row = hoja.createRow(++filasEscritas);
            for (int c = 0; c < fila.size() && c < numCols; c++) {
                Cell cell = row.createCell(c);
                escribirCelda(cell, fila.get(c), tipos[c], estiloMoneda, estiloFecha, estiloTexto);
            }
        }

        for (int i = 0; i < numCols; i++) {
            hoja.setColumnWidth(i, 4000);
        }
    }

    private boolean esFilaNull(List<Object> fila) {
        if (fila == null || fila.isEmpty())
            return true;
        boolean tieneContenido = false;
        for (Object v : fila) {
            if (v == null)
                continue;
            if (v instanceof String s) {
                String n = s.trim().toLowerCase(Locale.ROOT);
                if (n.isEmpty() || n.equals("null") || n.equals("#n/a") || n.equals("n/a") || n.equals("-")) {
                    continue;
                }
            }
            tieneContenido = true;
            break;
        }
        return !tieneContenido;
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
    // (Se mantiene exactamente igual)
    private void crearHojaResumen(Workbook wb, VistaCompleta vista, List<List<Object>> filasFiltradas,
            CellStyle estiloTitulo, CellStyle estiloSubtitulo, CellStyle estiloEncabezado, CellStyle estiloMoneda) {
        Sheet hoja = wb.createSheet("Resumen");
        int r = 0;

        Row t = hoja.createRow(r++);
        Cell ct = t.createCell(0);
        ct.setCellValue("Resumen financiero");
        ct.setCellStyle(estiloTitulo);

        ResumenMonetario resumen = calcularResumen(vista, filasFiltradas);
        r = filaTotales(hoja, r, "Total PEN", resumen.getTotalPEN().doubleValue(), estiloEncabezado, estiloMoneda);
        r = filaTotales(hoja, r, "Total USD", resumen.getTotalUSD().doubleValue(), estiloEncabezado, estiloMoneda);

        r += 2;

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

        Row tituloProv = hoja.createRow(r++);
        Cell ctp = tituloProv.createCell(0);
        ctp.setCellValue("Total de deuda por unidad operativa y proveedor");
        ctp.setCellStyle(estiloSubtitulo);

        Row encProv = hoja.createRow(r++);
        String[] colsProv = { "Unidad operativa", "RUC", "Proveedor", "Total PEN", "Total USD" };
        for (int i = 0; i < colsProv.length; i++) {
            Cell c = encProv.createCell(i);
            c.setCellValue(colsProv[i]);
            c.setCellStyle(estiloEncabezado);
        }

        List<ResumenProveedorUnidad> proveedores = calcularResumenPorProveedor(vista, filasFiltradas);
        for (ResumenProveedorUnidad rpu : proveedores) {
            Row row = hoja.createRow(r++);
            row.createCell(0).setCellValue(rpu.unidad == null ? "(sin unidad)" : rpu.unidad);
            row.createCell(1).setCellValue(rpu.ruc == null ? "" : rpu.ruc);
            row.createCell(2).setCellValue(rpu.proveedor == null ? "" : rpu.proveedor);
            Cell cPen = row.createCell(3);
            cPen.setCellValue(rpu.totalPEN.doubleValue());
            cPen.setCellStyle(estiloMoneda);
            Cell cUsd = row.createCell(4);
            cUsd.setCellValue(rpu.totalUSD.doubleValue());
            cUsd.setCellStyle(estiloMoneda);
        }

        for (int i = 0; i < 5; i++) {
            hoja.setColumnWidth(i, 5000);
        }
    }

    private int filaTotales(Sheet hoja, int r, String etiqueta, double valor, CellStyle e1, CellStyle e2) {
        Row row = hoja.createRow(r);
        Cell c1 = row.createCell(0);
        c1.setCellValue(etiqueta);
        c1.setCellStyle(e1);
        Cell c2 = row.createCell(1);
        c2.setCellValue(valor);
        c2.setCellStyle(e2);
        return r + 1;
    }

    // ------------------------------------------------------------------ Tratado
    // (Se mantiene exactamente igual, pero ahora agrupa las filas correctamente)
    private void crearHojaTratado(Workbook wb, VistaCompleta vista, List<List<Object>> filasFiltradas,
            CellStyle estiloEncabezado, CellStyle estiloMoneda, CellStyle estiloTexto, CellStyle estiloTotalFila) {
        Sheet hoja = wb.createSheet("Tratado");

        int idxUnidad = indiceColumna(vista, new String[] { "unidad operativa", "unidad", "uo" });
        int idxRuc = indiceColumna(vista, new String[] { "ruc" });
        int idxProveedor = indiceColumna(vista, new String[] { "proveedor" });
        int idxMoneda = indiceColumna(vista, new String[] { "divisa", "moneda", "currency" });
        int idxSaldoIngresado = indiceColumna(vista,
                new String[] { "saldo en moneda ingresada", "saldo moneda ingresada" });
        int idxSaldoUSD = indiceColumna(vista, new String[] { "saldo usd", "saldo en dolares", "saldo dolares" });
        int idxSaldoPEN = indiceColumna(vista, new String[] { "saldo pen", "saldo soles", "saldo en soles" });

        // Unidades y Proveedores únicos
        TreeSet<String> unidadesSet = new TreeSet<>();
        for (List<Object> fila : filasFiltradas) {
            if (esFilaNull(fila))
                continue;
            String u = idxUnidad >= 0 && fila.get(idxUnidad) != null ? fila.get(idxUnidad).toString().trim()
                    : "(sin unidad)";
            if (!u.isEmpty())
                unidadesSet.add(u);
        }
        List<String> unidades = new ArrayList<>(unidadesSet);

        TreeSet<String> proveedoresSet = new TreeSet<>();
        Map<String, String> proveedorNombre = new LinkedHashMap<>();
        for (List<Object> fila : filasFiltradas) {
            if (esFilaNull(fila))
                continue;
            String ruc = idxRuc >= 0 && fila.get(idxRuc) != null ? fila.get(idxRuc).toString().trim() : "";
            String prov = idxProveedor >= 0 && fila.get(idxProveedor) != null ? fila.get(idxProveedor).toString().trim()
                    : "";
            String clave = !ruc.isEmpty() ? ruc : (!prov.isEmpty() ? prov.toUpperCase(Locale.ROOT) : "(sin proveedor)");
            if (prov.isEmpty() && ruc.isEmpty())
                prov = "(sin proveedor)";
            proveedoresSet.add(clave);
            proveedorNombre.putIfAbsent(clave, prov);
        }
        List<String> proveedores = new ArrayList<>(proveedoresSet);

        // Agrupación
        Map<String, BigDecimal[]> acumulado = new LinkedHashMap<>();
        for (List<Object> fila : filasFiltradas) {
            if (esFilaNull(fila))
                continue;
            String unidad = idxUnidad >= 0 && fila.get(idxUnidad) != null ? fila.get(idxUnidad).toString().trim()
                    : "(sin unidad)";
            String ruc = idxRuc >= 0 && fila.get(idxRuc) != null ? fila.get(idxRuc).toString().trim() : "";
            String prov = idxProveedor >= 0 && fila.get(idxProveedor) != null ? fila.get(idxProveedor).toString().trim()
                    : "";
            String claveProv = !ruc.isEmpty() ? ruc
                    : (!prov.isEmpty() ? prov.toUpperCase(Locale.ROOT) : "(sin proveedor)");

            String clave = unidad + "||" + claveProv;
            BigDecimal[] acc = acumulado.computeIfAbsent(clave,
                    k -> new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO });

            String moneda = idxMoneda >= 0 && fila.get(idxMoneda) != null
                    ? fila.get(idxMoneda).toString().toUpperCase(Locale.ROOT)
                    : "";
            BigDecimal saldoIngresado = idxSaldoIngresado >= 0 ? aBigDecimal(fila.get(idxSaldoIngresado)) : null;
            BigDecimal saldoUSD = idxSaldoUSD >= 0 ? aBigDecimal(fila.get(idxSaldoUSD)) : null;
            BigDecimal saldoPEN = idxSaldoPEN >= 0 ? aBigDecimal(fila.get(idxSaldoPEN)) : null;

            if ("PEN".equals(moneda)) {
                BigDecimal v = saldoPEN != null ? saldoPEN : saldoIngresado;
                if (v != null)
                    acc[1] = acc[1].add(v);
            } else if ("USD".equals(moneda)) {
                BigDecimal v = saldoUSD != null ? saldoUSD : saldoIngresado;
                if (v != null)
                    acc[0] = acc[0].add(v);
            }
        }

        int colInicial = 1;
        int totalCols = 1 + unidades.size() * 2 + 2;
        int cTotalUsd = colInicial + unidades.size() * 2;
        int cTotalPen = cTotalUsd + 1;

        // Fila 0
        Row fila0 = hoja.createRow(0);
        Cell cEtiqueta0 = fila0.createCell(0);
        cEtiqueta0.setCellValue("Etiquetas de fila");
        cEtiqueta0.setCellStyle(estiloEncabezado);

        for (int u = 0; u < unidades.size(); u++) {
            int c = colInicial + u * 2;
            Cell cu = fila0.createCell(c);
            cu.setCellValue(unidades.get(u));
            cu.setCellStyle(estiloEncabezado);
            fila0.createCell(c + 1).setCellStyle(estiloEncabezado);
            hoja.addMergedRegion(new CellRangeAddress(0, 0, c, c + 1));
        }
        Cell ctu = fila0.createCell(cTotalUsd);
        ctu.setCellValue("Total Suma de Total USD");
        ctu.setCellStyle(estiloEncabezado);
        Cell ctp = fila0.createCell(cTotalPen);
        ctp.setCellValue("Total Suma de Total PEN");
        ctp.setCellStyle(estiloEncabezado);

        // Fila 1
        Row fila1 = hoja.createRow(1);
        fila1.createCell(0).setCellStyle(estiloEncabezado);
        for (int u = 0; u < unidades.size(); u++) {
            int c = colInicial + u * 2;
            Cell cUsd = fila1.createCell(c);
            cUsd.setCellValue("Suma de Total USD");
            cUsd.setCellStyle(estiloEncabezado);
            Cell cPen = fila1.createCell(c + 1);
            cPen.setCellValue("Suma de Total PEN");
            cPen.setCellStyle(estiloEncabezado);
        }
        Cell totUsd = fila1.createCell(cTotalUsd);
        totUsd.setCellValue("Total Suma de Total USD");
        totUsd.setCellStyle(estiloEncabezado);
        Cell totPen = fila1.createCell(cTotalPen);
        totPen.setCellValue("Total Suma de Total PEN");
        totPen.setCellStyle(estiloEncabezado);

        // Datos
        int r = 2;
        BigDecimal[] totalesPorColumna = new BigDecimal[totalCols];
        for (int i = 0; i < totalCols; i++)
            totalesPorColumna[i] = BigDecimal.ZERO;

        for (String provClave : proveedores) {
            Row row = hoja.createRow(r++);
            Cell nombre = row.createCell(0);
            nombre.setCellValue(proveedorNombre.getOrDefault(provClave, provClave));
            nombre.setCellStyle(estiloTexto);

            for (int u = 0; u < unidades.size(); u++) {
                String unidad = unidades.get(u);
                BigDecimal[] acc = acumulado.get(unidad + "||" + provClave);
                BigDecimal vUSD = acc != null ? acc[0] : BigDecimal.ZERO;
                BigDecimal vPEN = acc != null ? acc[1] : BigDecimal.ZERO;

                int c = colInicial + u * 2;
                Cell cUsd = row.createCell(c);
                cUsd.setCellValue(vUSD.doubleValue());
                cUsd.setCellStyle(estiloMoneda);
                Cell cPen = row.createCell(c + 1);
                cPen.setCellValue(vPEN.doubleValue());
                cPen.setCellStyle(estiloMoneda);

                totalesPorColumna[c] = totalesPorColumna[c].add(vUSD);
                totalesPorColumna[c + 1] = totalesPorColumna[c + 1].add(vPEN);
            }

            BigDecimal totalUSD = BigDecimal.ZERO, totalPEN = BigDecimal.ZERO;
            for (int u = 0; u < unidades.size(); u++) {
                BigDecimal[] acc = acumulado.get(unidades.get(u) + "||" + provClave);
                if (acc != null) {
                    totalUSD = totalUSD.add(acc[0]);
                    totalPEN = totalPEN.add(acc[1]);
                }
            }
            Cell cTotUsd = row.createCell(cTotalUsd);
            cTotUsd.setCellValue(totalUSD.doubleValue());
            cTotUsd.setCellStyle(estiloMoneda);
            Cell cTotPen = row.createCell(cTotalPen);
            cTotPen.setCellValue(totalPEN.doubleValue());
            cTotPen.setCellStyle(estiloMoneda);

            totalesPorColumna[cTotalUsd] = totalesPorColumna[cTotalUsd].add(totalUSD);
            totalesPorColumna[cTotalPen] = totalesPorColumna[cTotalPen].add(totalPEN);
        }

        Row filaTotal = hoja.createRow(r);
        Cell celdaTotal = filaTotal.createCell(0);
        celdaTotal.setCellValue("Total general");
        celdaTotal.setCellStyle(estiloTotalFila);
        for (int i = 1; i < totalCols; i++) {
            Cell c = filaTotal.createCell(i);
            c.setCellValue(totalesPorColumna[i].doubleValue());
            c.setCellStyle(estiloTotalFila);
        }

        // EL ANCHO CORRECTO PARA QUE NO SE DESBORDE NADA
        hoja.setColumnWidth(0, 10000); // Proveedor ancho
        for (int i = 1; i < totalCols; i++) {
            hoja.setColumnWidth(i, 4500); // Columnas de dinero parejas
        }
        hoja.createFreezePane(1, 2);
    }

    // ------------------------------------------------------------------ Cálculos
    // auxiliares

    private ResumenMonetario calcularResumen(VistaCompleta vista, List<List<Object>> filas) {
        int idxMoneda = indiceColumna(vista, new String[] { "divisa", "moneda", "currency" });
        int idxSaldoIngresado = indiceColumna(vista,
                new String[] { "saldo en moneda ingresada", "saldo moneda ingresada" });
        int idxSaldoUSD = indiceColumna(vista, new String[] { "saldo usd", "saldo en dolares", "saldo dolares" });
        int idxSaldoPEN = indiceColumna(vista, new String[] { "saldo pen", "saldo soles", "saldo en soles" });

        BigDecimal totalPEN = BigDecimal.ZERO, totalUSD = BigDecimal.ZERO;

        for (List<Object> fila : filas) {
            if (esFilaNull(fila))
                continue;
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

    private List<ResumenUnidad> calcularResumenPorUnidad(VistaCompleta vista, List<List<Object>> filas) {
        int idxUnidad = indiceColumna(vista, new String[] { "unidad operativa", "unidad", "uo" });
        int idxMoneda = indiceColumna(vista, new String[] { "divisa", "moneda", "currency" });
        int idxSaldoIngresado = indiceColumna(vista,
                new String[] { "saldo en moneda ingresada", "saldo moneda ingresada" });
        int idxSaldoUSD = indiceColumna(vista, new String[] { "saldo usd", "saldo en dolares", "saldo dolares" });
        int idxSaldoPEN = indiceColumna(vista, new String[] { "saldo pen", "saldo soles", "saldo en soles" });

        Map<String, ResumenUnidad> mapa = new LinkedHashMap<>();
        for (List<Object> fila : filas) {
            if (esFilaNull(fila))
                continue;
            String unidad = idxUnidad >= 0 && fila.get(idxUnidad) != null ? fila.get(idxUnidad).toString().trim()
                    : "(sin unidad)";
            ResumenUnidad ru = mapa.computeIfAbsent(unidad, ResumenUnidad::new);
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

    private List<ResumenProveedorUnidad> calcularResumenPorProveedor(VistaCompleta vista, List<List<Object>> filas) {
        int idxUnidad = indiceColumna(vista, new String[] { "unidad operativa", "unidad", "uo" });
        int idxRuc = indiceColumna(vista, new String[] { "ruc" });
        int idxProveedor = indiceColumna(vista, new String[] { "proveedor" });
        int idxMoneda = indiceColumna(vista, new String[] { "divisa", "moneda", "currency" });
        int idxSaldoIngresado = indiceColumna(vista,
                new String[] { "saldo en moneda ingresada", "saldo moneda ingresada" });
        int idxSaldoUSD = indiceColumna(vista, new String[] { "saldo usd", "saldo en dolares", "saldo dolares" });
        int idxSaldoPEN = indiceColumna(vista, new String[] { "saldo pen", "saldo soles", "saldo en soles" });

        Map<String, ResumenProveedorUnidad> mapa = new LinkedHashMap<>();
        for (List<Object> fila : filas) {
            if (esFilaNull(fila))
                continue;
            String unidad = idxUnidad >= 0 && fila.get(idxUnidad) != null ? fila.get(idxUnidad).toString().trim()
                    : "(sin unidad)";
            String ruc = idxRuc >= 0 && fila.get(idxRuc) != null ? fila.get(idxRuc).toString().trim() : "";
            String prov = idxProveedor >= 0 && fila.get(idxProveedor) != null ? fila.get(idxProveedor).toString().trim()
                    : "";
            String proveedorClave = !ruc.isEmpty() ? "RUC:" + ruc
                    : (!prov.isEmpty() ? "NOMBRE:" + prov.toUpperCase(Locale.ROOT) : "SIN");
            String clave = unidad + "||" + proveedorClave;

            ResumenProveedorUnidad rpu = mapa.computeIfAbsent(clave,
                    k -> new ResumenProveedorUnidad(unidad, ruc, prov.isEmpty() ? "(sin proveedor)" : prov));
            String moneda = idxMoneda >= 0 && fila.get(idxMoneda) != null
                    ? fila.get(idxMoneda).toString().toUpperCase(Locale.ROOT)
                    : "";
            BigDecimal saldoIngresado = idxSaldoIngresado >= 0 ? aBigDecimal(fila.get(idxSaldoIngresado)) : null;
            BigDecimal saldoUSD = idxSaldoUSD >= 0 ? aBigDecimal(fila.get(idxSaldoUSD)) : null;
            BigDecimal saldoPEN = idxSaldoPEN >= 0 ? aBigDecimal(fila.get(idxSaldoPEN)) : null;

            if ("PEN".equals(moneda)) {
                BigDecimal v = saldoPEN != null ? saldoPEN : saldoIngresado;
                if (v != null)
                    rpu.totalPEN = rpu.totalPEN.add(v);
            } else if ("USD".equals(moneda)) {
                BigDecimal v = saldoUSD != null ? saldoUSD : saldoIngresado;
                if (v != null)
                    rpu.totalUSD = rpu.totalUSD.add(v);
            }
        }
        List<ResumenProveedorUnidad> lista = new ArrayList<>(mapa.values());
        lista.sort((a, b) -> {
            int cu = a.unidad.compareTo(b.unidad);
            if (cu != 0)
                return cu;
            int cp = b.totalPEN.compareTo(a.totalPEN);
            if (cp != 0)
                return cp;
            return b.totalUSD.compareTo(a.totalUSD);
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
                .replaceAll("[áàäâ]", "a").replaceAll("[éèëê]", "e")
                .replaceAll("[íìïî]", "i").replaceAll("[óòöô]", "o")
                .replaceAll("[úùüû]", "u").replaceAll("[^a-z0-9]+", " ")
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
        s.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
        return s;
    }

    private CellStyle crearEstiloFecha(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setDataFormat(wb.createDataFormat().getFormat("dd/mm/yyyy"));
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
        s.setDataFormat(wb.createDataFormat().getFormat("@"));
        return s;
    }

    private CellStyle crearEstiloTotalFila(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        s.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
        return s;
    }

    private static class ResumenUnidad {
        final String unidad;
        BigDecimal totalPEN = BigDecimal.ZERO;
        BigDecimal totalUSD = BigDecimal.ZERO;

        ResumenUnidad(String u) {
            this.unidad = u;
        }
    }

    private static class ResumenProveedorUnidad {
        final String unidad;
        final String ruc;
        final String proveedor;
        BigDecimal totalPEN = BigDecimal.ZERO;
        BigDecimal totalUSD = BigDecimal.ZERO;

        ResumenProveedorUnidad(String unidad, String ruc, String proveedor) {
            this.unidad = unidad;
            this.ruc = ruc;
            this.proveedor = proveedor;
        }
    }
}