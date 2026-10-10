package com.cuentasclaras.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Filtros aplicados en la UI que deben respetarse al exportar.
 * Si un filtro está vacío, no se aplica.
 */
public class ExportarRequest {

    private String proveedor = "";
    private List<String> unidades = new ArrayList<>();
    private List<String> monedas = new ArrayList<>();

    public ExportarRequest() {
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public List<String> getUnidades() {
        return unidades;
    }

    public void setUnidades(List<String> unidades) {
        this.unidades = unidades;
    }

    public List<String> getMonedas() {
        return monedas;
    }

    public void setMonedas(List<String> monedas) {
        this.monedas = monedas;
    }
}