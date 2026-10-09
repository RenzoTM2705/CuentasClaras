package com.cuentasclaras.service;

import com.cuentasclaras.domain.MarcaDuplicado;
import com.cuentasclaras.domain.RegistroNormalizado;
import com.cuentasclaras.domain.ResumenCombinacion;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de deduplicación.
 *
 * <p>
 * Reglas clave:
 * </p>
 * <ul>
 * <li>Nunca elimina registros automáticamente.</li>
 * <li>Marca duplicados exactos y parciales para revisión.</li>
 * <li>Genera un resumen con contadores claros.</li>
 * </ul>
 *
 * <p>
 * Un duplicado es <b>exacto</b> si dos o más registros comparten la misma
 * clave de documento y tienen los mismos valores esenciales (importe, saldo,
 * fecha de vencimiento). Si los valores esenciales difieren, es <b>parcial</b>.
 * </p>
 */
@Service
public class DeduplicacionService {

    /**
     * Analiza los registros y marca duplicados. No elimina nada.
     * Devuelve un {@link ResumenCombinacion} con los contadores.
     */
    public ResumenCombinacion analizar(List<RegistroNormalizado> registros,
            int totalArchivos,
            List<String> advertenciasEstructurales) {

        ResumenCombinacion resumen = new ResumenCombinacion();
        resumen.setTotalArchivos(totalArchivos);
        resumen.setTotalRegistros(registros.size());
        if (advertenciasEstructurales != null) {
            resumen.setAdvertenciasEstructurales(new ArrayList<>(advertenciasEstructurales));
        }

        // Agrupar por clave de documento
        Map<String, List<RegistroNormalizado>> porClave = registros.stream()
                .filter(r -> r.getClaveDocumento() != null && !r.getClaveDocumento().isBlank())
                .collect(Collectors.groupingBy(RegistroNormalizado::getClaveDocumento));

        int unicos = 0;
        int duplicadosExactos = 0;
        int duplicadosParciales = 0;

        for (Map.Entry<String, List<RegistroNormalizado>> e : porClave.entrySet()) {
            List<RegistroNormalizado> grupo = e.getValue();
            if (grupo.size() == 1) {
                unicos++;
                continue;
            }

            // Grupo con múltiples registros: analizar si son exactos o parciales
            // Tomamos el primero como referencia
            RegistroNormalizado referencia = grupo.get(0);
            boolean todosIguales = grupo.stream().allMatch(r -> sonEsencialmenteIguales(referencia, r));

            if (todosIguales) {
                duplicadosExactos += grupo.size() - 1;
                unicos += 1; // el grupo cuenta como 1 único

                MarcaDuplicado marca = new MarcaDuplicado(
                        MarcaDuplicado.Tipo.EXACTO, e.getKey(), grupo.size());
                for (RegistroNormalizado r : grupo) {
                    r.setMarcaDuplicado(marca);
                }
            } else {
                // Grupo con variaciones: marcar todos como parciales
                duplicadosParciales += grupo.size();
                unicos += 0; // no los contamos como únicos, están en revisión

                MarcaDuplicado marca = new MarcaDuplicado(
                        MarcaDuplicado.Tipo.PARCIAL, e.getKey(), grupo.size());
                for (RegistroNormalizado r : grupo) {
                    r.setMarcaDuplicado(marca);
                }
            }
        }

        // Registros sin clave (no se pueden deduplicar): cuentan como únicos
        long sinClave = registros.stream()
                .filter(r -> r.getClaveDocumento() == null || r.getClaveDocumento().isBlank())
                .count();
        unicos += (int) sinClave;

        resumen.setRegistrosUnicos(unicos);
        resumen.setDuplicadosExactos(duplicadosExactos);
        resumen.setDuplicadosParciales(duplicadosParciales);
        return resumen;
    }

    /**
     * Compara dos registros por sus valores esenciales.
     * El criterio es: mismos importes, mismo saldo original, misma fecha de
     * vencimiento.
     */
    private boolean sonEsencialmenteIguales(RegistroNormalizado a, RegistroNormalizado b) {
        return eq(a.getImporteOriginal(), b.getImporteOriginal())
                && eq(a.getSaldoMonedaIngresada(), b.getSaldoMonedaIngresada())
                && eq(a.getSaldoPEN(), b.getSaldoPEN())
                && eq(a.getSaldoUSD(), b.getSaldoUSD())
                && Objects.equals(a.getFechaVencimiento(), b.getFechaVencimiento())
                && Objects.equals(a.getMoneda(), b.getMoneda());
    }

    private boolean eq(BigDecimal a, BigDecimal b) {
        if (a == null && b == null)
            return true;
        if (a == null || b == null)
            return false;
        return a.compareTo(b) == 0;
    }
}