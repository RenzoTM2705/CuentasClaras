package com.cuentasclaras.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Resumen del proceso de combinación y deduplicación.
 *
 * <p>
 * No persiste nada. Solo describe lo que ocurrió al procesar los archivos
 * en esta sesión.
 * </p>
 */
public class ResumenCombinacion {

    private int totalArchivos;
    private int totalRegistros;
    private int registrosUnicos;
    private int duplicadosExactos;
    private int duplicadosParciales;
    private List<String> advertenciasEstructurales = new ArrayList<>();

    public ResumenCombinacion() {
    }

    public int getTotalArchivos() {
        return totalArchivos;
    }

    public void setTotalArchivos(int totalArchivos) {
        this.totalArchivos = totalArchivos;
    }

    public int getTotalRegistros() {
        return totalRegistros;
    }

    public void setTotalRegistros(int totalRegistros) {
        this.totalRegistros = totalRegistros;
    }

    public int getRegistrosUnicos() {
        return registrosUnicos;
    }

    public void setRegistrosUnicos(int registrosUnicos) {
        this.registrosUnicos = registrosUnicos;
    }

    public int getDuplicadosExactos() {
        return duplicadosExactos;
    }

    public void setDuplicadosExactos(int duplicadosExactos) {
        this.duplicadosExactos = duplicadosExactos;
    }

    public int getDuplicadosParciales() {
        return duplicadosParciales;
    }

    public void setDuplicadosParciales(int duplicadosParciales) {
        this.duplicadosParciales = duplicadosParciales;
    }

    public List<String> getAdvertenciasEstructurales() {
        return advertenciasEstructurales;
    }

    public void setAdvertenciasEstructurales(List<String> v) {
        this.advertenciasEstructurales = v;
    }
}