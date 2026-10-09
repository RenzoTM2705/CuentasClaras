package com.cuentasclaras.importer;

import com.cuentasclaras.domain.Advertencia;
import com.cuentasclaras.domain.RegistroNormalizado;
import com.cuentasclaras.domain.ResultadoAnalisis;
import com.cuentasclaras.service.ClaveDocumentoService;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orquesta la importación de uno o varios Excel. Todo en memoria.
 */
@Service
public class ImportadorService {

    private final NormalizadorFilas normalizador;
    private final DetectorEncabezados detector;
    private final ClaveDocumentoService claves;

    public ImportadorService(NormalizadorFilas normalizador,
            DetectorEncabezados detector,
            ClaveDocumentoService claves) {
        this.normalizador = normalizador;
        this.detector = detector;
        this.claves = claves;
    }

    public ResultadoAnalisis importar(List<MultipartFile> archivos) {
        ResultadoAnalisis resultado = new ResultadoAnalisis();
        List<RegistroNormalizado> todos = new ArrayList<>();

        for (MultipartFile archivo : archivos) {
            procesarArchivo(archivo, resultado, todos);
        }

        detectarDuplicados(todos, resultado);

        resultado.setRegistros(todos);
        resultado.setTotalRegistrosValidos(todos.size());
        return resultado;
    }

    private void procesarArchivo(MultipartFile archivo,
            ResultadoAnalisis resultado,
            List<RegistroNormalizado> acumulador) {
        String nombre = archivo.getOriginalFilename();
        if (nombre == null)
            nombre = "sin_nombre";

        try (InputStream is = archivo.getInputStream();
                Workbook wb = WorkbookFactory.create(is)) {

            LectorExcel lector = new LectorExcel();
            lector.setWorkbook(wb);

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

                for (Map<String, Object> fila : filas) {
                    NormalizadorFilas.ResultadoNormalizacion res = normalizador.normalizar(fila, nombre,
                            hoja.getSheetName());
                    resultado.getAdvertencias().addAll(res.advertencias());

                    RegistroNormalizado r = res.registro();
                    claves.generar(r);
                    acumulador.add(r);
                }
            }

        } catch (Exception ex) {
            Advertencia w = new Advertencia(
                    "ARCHIVO_ILEGIBLE",
                    "No se pudo leer el archivo: " + ex.getMessage());
            w.setArchivo(nombre);
            resultado.getAdvertencias().add(w);
        }
    }

    private void detectarDuplicados(List<RegistroNormalizado> registros, ResultadoAnalisis resultado) {
        Map<String, Integer> conteo = new HashMap<>();
        for (RegistroNormalizado r : registros) {
            String clave = r.getClaveDocumento();
            if (clave == null)
                continue;
            conteo.merge(clave, 1, Integer::sum);
        }

        for (Map.Entry<String, Integer> e : conteo.entrySet()) {
            if (e.getValue() > 1) {
                Advertencia w = new Advertencia(
                        "DUPLICADO_POTENCIAL",
                        "Se detectaron " + e.getValue() + " registros con la misma clave. " +
                                "No se eliminan automáticamente; revísalos.");
                resultado.getAdvertencias().add(w);
            }
        }
    }
}