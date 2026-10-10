package com.cuentasclaras.domain;

public class Advertencia {

    private String codigo; // "COLUMNA_NO_RECONOCIDA", "FILA_INVALIDA", "DUPLICADO_AMBIGUO", etc.
    private String mensaje;
    private String archivo;
    private String hoja;
    private Integer fila;

    public Advertencia() {
    }

    public Advertencia(String codigo, String mensaje) {
        this.codigo = codigo;
        this.mensaje = mensaje;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getArchivo() {
        return archivo;
    }

    public void setArchivo(String archivo) {
        this.archivo = archivo;
    }

    public String getHoja() {
        return hoja;
    }

    public void setHoja(String hoja) {
        this.hoja = hoja;
    }

    public Integer getFila() {
        return fila;
    }

    public void setFila(Integer fila) {
        this.fila = fila;
    }
}