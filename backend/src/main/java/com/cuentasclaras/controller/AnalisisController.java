package com.cuentasclaras.controller;

import com.cuentasclaras.domain.ResultadoAnalisis;
import com.cuentasclaras.domain.SesionAnalisis;
import com.cuentasclaras.domain.VistaCompleta;
import com.cuentasclaras.domain.VistaGenerada;
import com.cuentasclaras.dto.AnalisisResponse;
import com.cuentasclaras.dto.ExportarRequest;
import com.cuentasclaras.importer.ImportadorService;
import com.cuentasclaras.service.ExportadorExcelService;
import com.cuentasclaras.service.GeneradorVistasService;
import com.cuentasclaras.service.SesionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AnalisisController {

    private final ImportadorService importador;
    private final GeneradorVistasService generadorVistas;
    private final SesionService sesiones;
    private final ExportadorExcelService exportador;

    public AnalisisController(ImportadorService importador,
            GeneradorVistasService generadorVistas,
            SesionService sesiones,
            ExportadorExcelService exportador) {
        this.importador = importador;
        this.generadorVistas = generadorVistas;
        this.sesiones = sesiones;
        this.exportador = exportador;
    }

    @PostMapping(value = "/analisis", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalisisResponse> analizar(
            @RequestParam("archivos") List<MultipartFile> archivos,
            @RequestParam(value = "modo", defaultValue = "combinar") String modo) {
        if (archivos == null || archivos.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        ResultadoAnalisis resultado = importador.importar(archivos);
        List<VistaGenerada> vistas = generadorVistas.generar(resultado.getRegistros(), modo);

        SesionAnalisis sesion = sesiones.crear(resultado.getRegistros(), vistas, resultado);

        AnalisisResponse response = new AnalisisResponse();
        response.setSessionId(sesion.getId());
        response.setTotalFilasLeidas(resultado.getTotalFilasLeidas());
        response.setTotalRegistrosValidos(resultado.getTotalRegistrosValidos());
        response.setTotalRegistrosRechazados(resultado.getTotalRegistrosRechazados());
        response.setVistas(vistas);
        response.setAdvertencias(resultado.getAdvertencias());
        response.setResumenCombinacion(resultado.getResumenCombinacion());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/sesiones/{sessionId}/vistas/{vistaId}")
    public ResponseEntity<VistaCompleta> obtenerVistaCompleta(
            @PathVariable String sessionId,
            @PathVariable String vistaId) {
        Optional<SesionAnalisis> sesionOpt = sesiones.obtener(sessionId);
        if (sesionOpt.isEmpty())
            return ResponseEntity.notFound().build();

        SesionAnalisis sesion = sesionOpt.get();

        Optional<VistaGenerada> vistaOpt = sesion.getVistas().stream()
                .filter(v -> v.getId().equals(vistaId))
                .findFirst();
        if (vistaOpt.isEmpty())
            return ResponseEntity.notFound().build();

        VistaCompleta completa = generadorVistas.construirCompleta(
                vistaOpt.get(), sesion.getRegistros());
        return ResponseEntity.ok(completa);
    }

    @PostMapping("/sesiones/{sessionId}/vistas/{vistaId}/exportar")
    public ResponseEntity<byte[]> exportar(
            @PathVariable String sessionId,
            @PathVariable String vistaId,
            @RequestBody(required = false) ExportarRequest filtros) {
        Optional<SesionAnalisis> sesionOpt = sesiones.obtener(sessionId);
        if (sesionOpt.isEmpty())
            return ResponseEntity.notFound().build();

        SesionAnalisis sesion = sesionOpt.get();

        Optional<VistaGenerada> vistaOpt = sesion.getVistas().stream()
                .filter(v -> v.getId().equals(vistaId))
                .findFirst();
        if (vistaOpt.isEmpty())
            return ResponseEntity.notFound().build();

        VistaCompleta completa = generadorVistas.construirCompleta(
                vistaOpt.get(), sesion.getRegistros());

        byte[] xlsx = exportador.exportar(completa, filtros);

        String nombre = construirNombreArchivo(completa);
        String contentDisposition = "attachment; filename=\"" + nombre + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .header(HttpHeaders.CONTENT_TYPE,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(xlsx);
    }

    private String construirNombreArchivo(VistaCompleta vista) {
        String base = vista.getNombre() == null ? "vista" : vista.getNombre();
        // Reemplazar caracteres problemáticos para nombres de archivo
        base = base.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        if (base.isEmpty())
            base = "vista";
        return base + ".xlsx";
    }
}