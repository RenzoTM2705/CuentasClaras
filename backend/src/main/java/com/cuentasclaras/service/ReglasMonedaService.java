package com.cuentasclaras.service;

import com.cuentasclaras.domain.RegistroNormalizado;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Reglas de moneda: PEN y USD nunca se suman. No se inventan tipos de cambio.
 */
@Service
public class ReglasMonedaService {

    public enum Moneda {
        PEN, USD, DESCONOCIDA
    }

    public Moneda monedaDe(RegistroNormalizado r) {
        String m = r.getMoneda();
        if (m == null)
            return Moneda.DESCONOCIDA;
        String n = m.trim().toUpperCase();
        if (n.equals("PEN") || n.equals("SOLES") || n.equals("S/") || n.equals("S"))
            return Moneda.PEN;
        if (n.equals("USD") || n.equals("DOLARES") || n.equals("$") || n.equals("US$"))
            return Moneda.USD;
        return Moneda.DESCONOCIDA;
    }

    public BigDecimal saldoOriginal(RegistroNormalizado r) {
        return switch (monedaDe(r)) {
            case PEN -> primerNoNulo(r.getSaldoPEN(), r.getSaldoMonedaIngresada(), r.getSaldoFuncionalPEN());
            case USD -> primerNoNulo(r.getSaldoUSD(), r.getSaldoMonedaIngresada());
            case DESCONOCIDA -> primerNoNulo(r.getSaldoMonedaIngresada(), r.getSaldoPEN(), r.getSaldoUSD());
        };
    }

    public BigDecimal saldoFuncionalPEN(RegistroNormalizado r) {
        return r.getSaldoFuncionalPEN();
    }

    public boolean equivalenciaPENPendiente(RegistroNormalizado r) {
        return monedaDe(r) == Moneda.USD && r.getSaldoFuncionalPEN() == null;
    }

    private BigDecimal primerNoNulo(BigDecimal... valores) {
        for (BigDecimal v : valores) {
            if (v != null)
                return v;
        }
        return null;
    }
}