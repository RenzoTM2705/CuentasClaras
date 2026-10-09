package com.cuentasclaras.domain;

/**
 * Marca que un registro tiene una clave de documento repetida.
 *
 * <p>
 * No se elimina automáticamente ningún registro. Solo se marca para que
 * el usuario lo revise.
 * </p>
 */
public class MarcaDuplicado {

    public enum Tipo {
        /**
         * Misma clave y mismos valores esenciales. Muy probablemente el mismo
         * documento.
         */
        EXACTO,
        /**
         * Misma clave pero valores distintos (importe, fecha, saldo...). Requiere
         * revisión.
         */
        PARCIAL
    }

    private Tipo tipo;
    private String clave;
    private int totalRegistrosConMismaClave;

    public MarcaDuplicado() {
    }

    public MarcaDuplicado(Tipo tipo, String clave, int total) {
        this.tipo = tipo;
        this.clave = clave;
        this.totalRegistrosConMismaClave = total;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public int getTotalRegistrosConMismaClave() {
        return totalRegistrosConMismaClave;
    }

    public void setTotalRegistrosConMismaClave(int t) {
        this.totalRegistrosConMismaClave = t;
    }
}