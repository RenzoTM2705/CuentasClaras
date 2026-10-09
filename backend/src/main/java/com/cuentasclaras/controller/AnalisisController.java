package com.cuentasclaras.controller;

import com.cuentasclaras.domain.Advertencia;
import com.cuentasclaras.domain.ResumenMonetario;
import com.cuentasclaras.domain.VistaGenerada;
import com.cuentasclaras.dto.AnalisisRequest;
import com.cuentasclaras.dto.AnalisisResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AnalisisController {

    /**
     * Endpoint provisional. En Fase 2 devuelve datos simulados para validar el
     * contrato de la API con el frontend. En Fase 3 se reemplazará por el
     * procesamiento real de archivos Excel subidos.
     */
    @PostMapping("/analisis")
    public ResponseEntity<AnalisisResponse> analizar(@RequestBody(required = false) AnalisisRequest request) {

        VistaGenerada consolidado = new VistaGenerada();
        consolidado.setId("consolidado");
        consolidado.setNombre("Consolidado");
        consolidado.setTipo("consolidado");
        consolidado.setTotalDocumentos(0);
        consolidado.setColumnas(List.of());
        consolidado.setFilas(List.of());

        ResumenMonetario resumen = new ResumenMonetario();
        resumen.setTotalPEN(BigDecimal.ZERO);
        resumen.setTotalUSD(BigDecimal.ZERO);
        consolidado.setResumen(resumen);

        AnalisisResponse response = new AnalisisResponse();
        response.setTotalFilasLeidas(0);
        response.setTotalRegistrosValidos(0);
        response.setTotalRegistrosRechazados(0);
        response.setVistas(List.of(consolidado));
        response.setAdvertencias(List.of(
                new Advertencia("FASE_2", "Endpoint provisional. El procesamiento real llega en Fase 3.")));

        return ResponseEntity.ok(response);
    }
}