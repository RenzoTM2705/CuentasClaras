package com.cuentasclaras.domain;

import java.util.List;

public class VistaGenerada {

    private String id; // identificador único de la vista
    private String nombre; // nombre mostrado
    private String tipo; // "consolidado", "unidad", "archivo", "proveedor"
    private int totalDocumentos;
    private ResumenMonetario resumen;
    private List<String> columnas; // encabezados tal como se mostrarán
    private List<List<Object>> filas; // previsualización (limitada)

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