package com.cuentasclaras.domain;

import java.math.BigDecimal;

/**
 * Resumen de saldos agrupado por proveedor dentro de una vista.
 *
 * <p>
 * Los totales PEN y USD se mantienen separados. Nunca se suman entre sí.
 * </p>
 */
public class ResumenProveedor {

    private String ruc;
    private String proveedor;
    private int totalDocumentos;
    private BigDecimal totalPEN = BigDecimal.ZERO;
    private BigDecimal totalUSD = BigDecimal.ZERO;
    private int registrosConEquivalenciaPENPendiente = 0;

    public ResumenProveedor() {
    }

    public ResumenProveedor(String ruc, String proveedor) {
        this.ruc = ruc;
        this.proveedor = proveedor;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public int getTotalDocumentos() {
        return totalDocumentos;
    }

    public void setTotalDocumentos(int totalDocumentos) {
        this.totalDocumentos = totalDocumentos;
    }

    public BigDecimal getTotalPEN() {
        return totalPEN;
    }

    public void setTotalPEN(BigDecimal totalPEN) {
        this.totalPEN = totalPEN;
    }

    public BigDecimal getTotalUSD() {
        return totalUSD;
    }

    public void setTotalUSD(BigDecimal totalUSD) {
        this.totalUSD = totalUSD;
    }

    public int getRegistrosConEquivalenciaPENPendiente() {
        return registrosConEquivalenciaPENPendiente;
    }

    public void setRegistrosConEquivalenciaPENPendiente(int v) {
        this.registrosConEquivalenciaPENPendiente = v;
    }
}