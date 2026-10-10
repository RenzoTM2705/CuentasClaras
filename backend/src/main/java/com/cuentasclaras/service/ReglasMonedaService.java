package com.cuentasclaras.service;

import com.cuentasclaras.domain.RegistroNormalizado;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Locale;

/**
 * Reglas de moneda: PEN y USD nunca se suman. No se inventan tipos de cambio.
 */
@Service
public class ReglasMonedaService {

    public enum Moneda {
        PEN, USD, DESCONOCIDA
    }

    public Moneda monedaDe(RegistroNormalizado r) {
        if (r == null)
            return Moneda.DESCONOCIDA;
        String m = r.getMoneda();
        if (m == null)
            return Moneda.DESCONOCIDA;

        String n = normalizar(m);
        if (n.equals("pen") || n.equals("soles") || n.equals("s") || n.equals("s/")) {
            return Moneda.PEN;
        }
        if (n.equals("usd") || n.equals("dolares") || n.equals("$") || n.equals("us$")
                || n.equals("usd$") || n.equals("dolar")) {
            return Moneda.USD;
        }
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

    /**
     * Normaliza una cadena: minúsculas, sin tildes, sin espacios extra.
     */
    private String normalizar(String s) {
        if (s == null)
            return "";
        String sinTildes = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT).trim();
    }
}