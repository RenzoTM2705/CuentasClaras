package com.cuentasclaras.controller;

import com.cuentasclaras.domain.ResultadoAnalisis;
import com.cuentasclaras.domain.VistaGenerada;
import com.cuentasclaras.dto.AnalisisResponse;
import com.cuentasclaras.importer.ImportadorService;
import com.cuentasclaras.service.GeneradorVistasService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AnalisisController {

    private final ImportadorService importador;
    private final GeneradorVistasService generadorVistas;

    public AnalisisController(ImportadorService importador,
            GeneradorVistasService generadorVistas) {
        this.importador = importador;
        this.generadorVistas = generadorVistas;
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

        AnalisisResponse response = new AnalisisResponse();
        response.setTotalFilasLeidas(resultado.getTotalFilasLeidas());
        response.setTotalRegistrosValidos(resultado.getTotalRegistrosValidos());
        response.setTotalRegistrosRechazados(resultado.getTotalRegistrosRechazados());
        response.setVistas(vistas);
        response.setAdvertencias(resultado.getAdvertencias());
        response.setResumenCombinacion(resultado.getResumenCombinacion());
        
        return ResponseEntity.ok(response);
    }
}