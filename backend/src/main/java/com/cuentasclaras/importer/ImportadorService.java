package com.cuentasclaras.importer;

import com.cuentasclaras.domain.Advertencia;
import com.cuentasclaras.domain.RegistroNormalizado;
import com.cuentasclaras.domain.ResultadoAnalisis;
import com.cuentasclaras.domain.ResumenCombinacion;
import com.cuentasclaras.service.ClaveDocumentoService;
import com.cuentasclaras.service.DeduplicacionService;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Orquesta la importación de uno o varios Excel. Todo en memoria.
 */
@Service
public class ImportadorService {

    private final NormalizadorFilas normalizador;
    private final DetectorEncabezados detector;
    private final ClaveDocumentoService claves;
    private final DeduplicacionService deduplicacion;

    public ImportadorService(NormalizadorFilas normalizador,
            DetectorEncabezados detector,
            ClaveDocumentoService claves,
            DeduplicacionService deduplicacion) {
        this.normalizador = normalizador;
        this.detector = detector;
        this.claves = claves;
        this.deduplicacion = deduplicacion;
    }

    public ResultadoAnalisis importar(List<MultipartFile> archivos) {
        ResultadoAnalisis resultado = new ResultadoAnalisis();
        List<RegistroNormalizado> todos = new ArrayList<>();

        // Detectar incompatibilidades estructurales entre archivos
        List<String> advertenciasEstructurales = new ArrayList<>();
        Map<String, Set<String>> columnasPorArchivo = new java.util.LinkedHashMap<>();

        for (MultipartFile archivo : archivos) {
            procesarArchivo(archivo, resultado, todos, columnasPorArchivo);
        }

        // Comparar columnas entre archivos
        detectarIncompatibilidades(columnasPorArchivo, advertenciasEstructurales, resultado);

        // Deduplicación fina (no elimina, marca)
        ResumenCombinacion resumen = deduplicacion.analizar(
                todos, archivos.size(), advertenciasEstructurales);

        resultado.setRegistros(todos);
        resultado.setTotalRegistrosValidos(todos.size());
        resultado.setResumenCombinacion(resumen);
        return resultado;
    }

    private void procesarArchivo(MultipartFile archivo,
            ResultadoAnalisis resultado,
            List<RegistroNormalizado> acumulador,
            Map<String, Set<String>> columnasPorArchivo) {
        String nombre = archivo.getOriginalFilename();
        if (nombre == null)
            nombre = "sin_nombre";

        try (InputStream is = archivo.getInputStream();
                Workbook wb = WorkbookFactory.create(is)) {

            LectorExcel lector = new LectorExcel();
            lector.setWorkbook(wb);

            Set<String> columnasDetectadas = new LinkedHashSet<>();

            for (int s = 0; s < wb.getNumberOfSheets(); s++) {
                Sheet hoja = wb.getSheetAt(s);
                if (hoja.getPhysicalNumberOfRows() == 0)
                    continue;

                DetectorEncabezados.ResultadoDetector det = detector.detectar(hoja);

                if (det.confianza() == DetectorEncabezados.Confianza.BAJA || det.filaEncabezado() < 0) {
                    Advertencia w = new Advertencia(
                            "ENCABEZADO_NO_DETECTADO",
                            "No se pudo detectar una fila de encabezados. Hoja omitida.");
                    w.setArchivo(nombre);
                    w.setHoja(hoja.getSheetName());
                    resultado.getAdvertencias().add(w);
                    continue;
                }

                if (det.mapeoColumnas().isEmpty()) {
                    Advertencia w = new Advertencia(
                            "HOJA_SIN_COLUMNAS_RECONOCIDAS",
                            "La hoja no contiene ninguna columna reconocible del catálogo. Se omite.");
                    w.setArchivo(nombre);
                    w.setHoja(hoja.getSheetName());
                    resultado.getAdvertencias().add(w);
                    continue;
                }

                if (det.confianza() == DetectorEncabezados.Confianza.MEDIA) {
                    Advertencia w = new Advertencia(
                            "ENCABEZADO_CONFIANZA_MEDIA",
                            "Detección de encabezados con confianza media. Revisa el mapeo.");
                    w.setArchivo(nombre);
                    w.setHoja(hoja.getSheetName());
                    w.setFila(det.filaEncabezado() + 1);
                    resultado.getAdvertencias().add(w);
                }

                List<Map<String, Object>> filas = lector.leerDesde(hoja, det.filaEncabezado());
                resultado.setTotalFilasLeidas(resultado.getTotalFilasLeidas() + filas.size());

                // Extraer la fecha del reporte (fila 5 usualmente)
                String fechaReporte = extraerFechaReporte(hoja);

                for (Map<String, Object> fila : filas) {
                    // Registrar columnas para detectar incompatibilidades
                    for (String k : fila.keySet()) {
                        if (!"__filaOrigen".equals(k))
                            columnasDetectadas.add(k);
                    }

                    NormalizadorFilas.ResultadoNormalizacion res = normalizador.normalizar(fila, nombre,
                            hoja.getSheetName());
                    resultado.getAdvertencias().addAll(res.advertencias());

                    RegistroNormalizado r = res.registro();
                    r.setFechaReporte(fechaReporte); // <--- ASIGNACIÓN CORRECTA DENTRO DEL BUCLE
                    claves.generar(r);
                    acumulador.add(r);
                }
            }

            if (!columnasDetectadas.isEmpty()) {
                columnasPorArchivo.put(nombre, columnasDetectadas);
            }

        } catch (Exception ex) {
            Advertencia w = new Advertencia(
                    "ARCHIVO_ILEGIBLE",
                    "No se pudo leer el archivo: " + ex.getClass().getSimpleName()
                            + " - " + ex.getMessage());
            w.setArchivo(nombre);
            resultado.getAdvertencias().add(w);
        }
    }

    /**
     * Compara las columnas detectadas en cada archivo.
     * Si dos archivos comparten muy pocas columnas, informarlo.
     * No bloquea el procesamiento: solo añade advertencias.
     */
    private void detectarIncompatibilidades(Map<String, Set<String>> columnasPorArchivo,
            List<String> advertenciasEstructurales,
            ResultadoAnalisis resultado) {
        if (columnasPorArchivo.size() < 2)
            return;

        List<Map.Entry<String, Set<String>>> entries = new ArrayList<>(columnasPorArchivo.entrySet());

        for (int i = 0; i < entries.size(); i++) {
            for (int j = i + 1; j < entries.size(); j++) {
                String a = entries.get(i).getKey();
                String b = entries.get(j).getKey();
                Set<String> colA = entries.get(i).getValue();
                Set<String> colB = entries.get(j).getValue();

                Set<String> interseccion = new java.util.HashSet<>(colA);
                interseccion.retainAll(colB);

                if (interseccion.isEmpty()) {
                    String msg = "Los archivos '" + a + "' y '" + b
                            + "' no comparten ninguna columna. Se combinarán igualmente, pero revisa el resultado.";
                    advertenciasEstructurales.add(msg);
                    Advertencia w = new Advertencia("ARCHIVOS_INCOMPATIBLES", msg);
                    w.setArchivo(a);
                    resultado.getAdvertencias().add(w);
                } else if (interseccion.size() < 3) {
                    String msg = "Los archivos '" + a + "' y '" + b
                            + "' comparten solo " + interseccion.size()
                            + " columnas. La combinación puede ser incompleta.";
                    advertenciasEstructurales.add(msg);
                    Advertencia w = new Advertencia("COMPATIBILIDAD_BAJA", msg);
                    w.setArchivo(a);
                    resultado.getAdvertencias().add(w);
                }
            }
        }
    }

    /**
     * Busca una celda en las primeras 10 filas que contenga "AL:" o "AL." y extrae
     * la fecha.
     */
        private String extraerFechaReporte(Sheet hoja) {
        DataFormatter formatter = new DataFormatter();
        
        for (int i = 0; i < Math.min(15, hoja.getLastRowNum()); i++) {
            Row row = hoja.getRow(i);
            if (row == null) continue;
            
            for (int c = 0; c < row.getLastCellNum(); c++) {
                Cell cell = row.getCell(c);
                if (cell != null) {
                    String val = formatter.formatCellValue(cell).trim().toUpperCase();
                    
                    // Si la celda dice "AL." o "AL:"
                    if (val.equals("AL.") || val.equals("AL:") || val.startsWith("AL. ") || val.startsWith("AL: ")) {
                        // 1. Intentar extraer la fecha de la misma celda (ej. "AL. 05-10-2026")
                        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{1,2}[-./]\\d{1,2}[-./]\\d{2,4})").matcher(val);
                        if (m.find()) return m.group(1);
                        
                        // 2. Si no está en la misma celda, leer la celda de al lado
                        Cell nextCell = row.getCell(c + 1);
                        if (nextCell != null) {
                            String nextVal = formatter.formatCellValue(nextCell).trim();
                            if (!nextVal.isEmpty()) return nextVal;
                        }
                    }
                }
            }
        }
        return "Sin fecha";
    }
}