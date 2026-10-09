package com.cuentasclaras.dto;

/**
 * Request de análisis. En Fase 2 sirve como contrato provisional.
 * En Fase 3 se sustituirá por una subida multipart real.
 */
public class AnalisisRequest {

    private String modo; // "combinar", "independiente", "ambos"

    public AnalisisRequest() {
    }

    public String getModo() {
        return modo;
    }

    public void setModo(String modo) {
        this.modo = modo;
    }
}