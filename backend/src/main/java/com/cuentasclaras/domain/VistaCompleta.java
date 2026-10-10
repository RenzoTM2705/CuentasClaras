package com.cuentasclaras.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Vista con todas las filas, para servirse bajo demanda.
 */
public class VistaCompleta {

    private String id;
    private String nombre;
    private String tipo;
    private int totalDocumentos;
    private ResumenMonetario resumen;
    private List<ResumenProveedor> resumenPorProveedor = new ArrayList<>();
    private List<String> unidadesOperativas = new ArrayList<>();
    private List<String> columnas = new ArrayList<>();
    private List<List<Object>> filas = new ArrayList<>();

    public VistaCompleta() {
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

    public List<List<Object>> getFilas() {
        return filas;
    }

    public void setFilas(List<List<Object>> filas) {
        this.filas = filas;
    }
}