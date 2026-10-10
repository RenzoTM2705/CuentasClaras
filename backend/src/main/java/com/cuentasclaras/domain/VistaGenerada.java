package com.cuentasclaras.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Vista de análisis expuesta al frontend.
 *
 * <p>
 * En el catálogo (respuesta de POST /api/analisis) solo se envían
 * las primeras {@code filasPreview} filas. La vista completa se obtiene
 * bajo demanda con GET /api/vistas/{id}.
 * </p>
 */
public class VistaGenerada {

    private String id;
    private String nombre;
    private String tipo; // "archivo" o "combinado"
    private int totalDocumentos;
    private ResumenMonetario resumen;
    private List<ResumenProveedor> resumenPorProveedor = new ArrayList<>();
    private List<String> unidadesOperativas = new ArrayList<>();
    private List<String> columnas = new ArrayList<>();

    /** Solo las primeras filas, para el mosaico. */
    private List<List<Object>> filasPreview = new ArrayList<>();

    public VistaGenerada() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getTotalDocumentos() {
        return totalDocumentos;
    }

    public void setTotalDocumentos(int totalDocumentos) {
        this.totalDocumentos = totalDocumentos;
    }

    public ResumenMonetario getResumen() {
        return resumen;
    }

    public void setResumen(ResumenMonetario resumen) {
        this.resumen = resumen;
    }

    public List<ResumenProveedor> getResumenPorProveedor() {
        return resumenPorProveedor;
    }

    public void setResumenPorProveedor(List<ResumenProveedor> r) {
        this.resumenPorProveedor = r;
    }

    public List<String> getUnidadesOperativas() {
        return unidadesOperativas;
    }

    public void setUnidadesOperativas(List<String> u) {
        this.unidadesOperativas = u;
    }

    public List<String> getColumnas() {
        return columnas;
    }

    public void setColumnas(List<String> columnas) {
        this.columnas = columnas;
    }

    public List<List<Object>> getFilasPreview() {
        return filasPreview;
    }

    public void setFilasPreview(List<List<Object>> filasPreview) {
        this.filasPreview = filasPreview;
    }
}