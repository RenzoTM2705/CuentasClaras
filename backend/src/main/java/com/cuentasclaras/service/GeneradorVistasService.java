package com.cuentasclaras.service;

import com.cuentasclaras.domain.RegistroNormalizado;
import com.cuentasclaras.domain.ResumenMonetario;
import com.cuentasclaras.domain.VistaGenerada;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Genera las vistas (consolidado, por archivo, por unidad operativa).
 * El resumen mantiene PEN y USD separados.
 */
@Service
public class GeneradorVistasService {

    private static final int MAX_FILAS_PREVIEW = 50;

    private final ReglasMonedaService moneda;

    public GeneradorVistasService(ReglasMonedaService moneda) {
        this.moneda = moneda;
    }

    public List<VistaGenerada> generar(List<RegistroNormalizado> registros, String modo) {
        List<VistaGenerada> vistas = new ArrayList<>();

        if ("combinar".equals(modo) || "ambos".equals(modo)) {
            vistas.add(construir("consolidado", "Consolidado", "consolidado", registros));
        }

        if ("independiente".equals(modo) || "ambos".equals(modo)) {
            Map<String, List<RegistroNormalizado>> porArchivo = registros.stream()
                    .collect(Collectors.groupingBy(
                            r -> r.getArchivoOrigen() == null ? "sin_archivo" : r.getArchivoOrigen(),
                            LinkedHashMap::new,
                            Collectors.toList()));
            for (Map.Entry<String, List<RegistroNormalizado>> e : porArchivo.entrySet()) {
                vistas.add(construir("archivo:" + e.getKey(), e.getKey(), "archivo", e.getValue()));
            }
        }

        Map<String, List<RegistroNormalizado>> porUnidad = registros.stream()
                .filter(r -> r.getUnidadOperativa() != null && !r.getUnidadOperativa().isBlank())
                .collect(Collectors.groupingBy(
                        RegistroNormalizado::getUnidadOperativa,
                        LinkedHashMap::new,
                        Collectors.toList()));
        if (porUnidad.size() > 1) {
            for (Map.Entry<String, List<RegistroNormalizado>> e : porUnidad.entrySet()) {
                vistas.add(construir("unidad:" + e.getKey(), "Unidad " + e.getKey(), "unidad", e.getValue()));
            }
        }

        return vistas;
    }

    private VistaGenerada construir(String id, String nombre, String tipo,
            List<RegistroNormalizado> registros) {
        VistaGenerada v = new VistaGenerada();
        v.setId(id);
        v.setNombre(nombre);
        v.setTipo(tipo);
        v.setTotalDocumentos(registros.size());
        v.setResumen(construirResumen(registros));
        v.setColumnas(columnasDe(registros));
        v.setFilas(previewFilas(registros));
        return v;
    }

    private ResumenMonetario construirResumen(List<RegistroNormalizado> registros) {
        ResumenMonetario r = new ResumenMonetario();
        BigDecimal totalPEN = BigDecimal.ZERO;
        BigDecimal totalUSD = BigDecimal.ZERO;
        BigDecimal totalFuncionalPEN = BigDecimal.ZERO;
        int pendientes = 0;

        for (RegistroNormalizado reg : registros) {
            ReglasMonedaService.Moneda m = moneda.monedaDe(reg);
            BigDecimal saldoOrig = moneda.saldoOriginal(reg);

            if (m == ReglasMonedaService.Moneda.PEN && saldoOrig != null) {
                totalPEN = totalPEN.add(saldoOrig);
            } else if (m == ReglasMonedaService.Moneda.USD && saldoOrig != null) {
                totalUSD = totalUSD.add(saldoOrig);
            }

            BigDecimal funcional = moneda.saldoFuncionalPEN(reg);
            if (funcional != null) {
                totalFuncionalPEN = totalFuncionalPEN.add(funcional);
            }

            if (moneda.equivalenciaPENPendiente(reg))
                pendientes++;
        }

        r.setTotalPEN(totalPEN);
        r.setTotalUSD(totalUSD);
        r.setTotalFuncionalPENConfirmado(totalFuncionalPEN);
        r.setRegistrosConEquivalenciaPENPendiente(pendientes);
        return r;
    }

    private List<String> columnasDe(List<RegistroNormalizado> registros) {
        for (RegistroNormalizado r : registros) {
            if (r.getValoresOriginales() != null && !r.getValoresOriginales().isEmpty()) {
                return r.getValoresOriginales().keySet().stream()
                        .filter(k -> !"__filaOrigen".equals(k))
                        .toList();
            }
        }
        return List.of();
    }

    private List<List<Object>> previewFilas(List<RegistroNormalizado> registros) {
        List<List<Object>> filas = new ArrayList<>();
        int limite = Math.min(registros.size(), MAX_FILAS_PREVIEW);
        for (int i = 0; i < limite; i++) {
            RegistroNormalizado r = registros.get(i);
            if (r.getValoresOriginales() == null) {
                filas.add(List.of());
                continue;
            }
            List<Object> fila = new ArrayList<>();
            for (Map.Entry<String, Object> e : r.getValoresOriginales().entrySet()) {
                if ("__filaOrigen".equals(e.getKey()))
                    continue;
                fila.add(e.getValue());
            }
            filas.add(fila);
        }
        return filas;
    }
}