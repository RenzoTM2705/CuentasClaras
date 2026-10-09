package com.cuentasclaras.domain;

import java.math.BigDecimal;

public class ResumenMonetario {

    private BigDecimal totalPEN = BigDecimal.ZERO;
    private BigDecimal totalUSD = BigDecimal.ZERO;
    private BigDecimal totalFuncionalPENConfirmado = BigDecimal.ZERO;
    private int registrosConEquivalenciaPENPendiente = 0;

    public ResumenMonetario() {
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

    public BigDecimal getTotalFuncionalPENConfirmado() {
        return totalFuncionalPENConfirmado;
    }

    public void setTotalFuncionalPENConfirmado(BigDecimal v) {
        this.totalFuncionalPENConfirmado = v;
    }

    public int getRegistrosConEquivalenciaPENPendiente() {
        return registrosConEquivalenciaPENPendiente;
    }

    public void setRegistrosConEquivalenciaPENPendiente(int v) {
        this.registrosConEquivalenciaPENPendiente = v;
    }
}