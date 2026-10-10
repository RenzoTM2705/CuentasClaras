package com.cuentasclaras.service;

import com.cuentasclaras.domain.RegistroNormalizado;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ReglasMonedaServiceTest {

    private final ReglasMonedaService service = new ReglasMonedaService();

    @Test
    void monedaPENConColumnasUSDVaciasSeProcesaNormal() {
        RegistroNormalizado r = new RegistroNormalizado();
        r.setMoneda("PEN");
        r.setSaldoPEN(new BigDecimal("1500.00"));
        // saldoUSD queda null: no es un error

        assertThat(service.monedaDe(r)).isEqualTo(ReglasMonedaService.Moneda.PEN);
        assertThat(service.saldoOriginal(r)).isEqualByComparingTo("1500.00");
        assertThat(service.equivalenciaPENPendiente(r)).isFalse();
    }

    @Test
    void monedaUSDSinSaldoFuncionalPENQuedaPendiente() {
        RegistroNormalizado r = new RegistroNormalizado();
        r.setMoneda("USD");
        r.setSaldoUSD(new BigDecimal("800.00"));
        // saldoFuncionalPEN null

        assertThat(service.monedaDe(r)).isEqualTo(ReglasMonedaService.Moneda.USD);
        assertThat(service.saldoOriginal(r)).isEqualByComparingTo("800.00");
        assertThat(service.equivalenciaPENPendiente(r)).isTrue();
    }

    @Test
    void monedaUSDConSaldoFuncionalPENNoEsPendiente() {
        RegistroNormalizado r = new RegistroNormalizado();
        r.setMoneda("USD");
        r.setSaldoUSD(new BigDecimal("800.00"));
        r.setSaldoFuncionalPEN(new BigDecimal("3000.00"));

        assertThat(service.equivalenciaPENPendiente(r)).isFalse();
    }

    @Test
    void divisaEscritaComoSolesSeReconoceComoPEN() {
        RegistroNormalizado r = new RegistroNormalizado();
        r.setMoneda("Soles");

        assertThat(service.monedaDe(r)).isEqualTo(ReglasMonedaService.Moneda.PEN);
    }

    @Test
    void divisaEscritaComoDolaresSeReconoceComoUSD() {
        RegistroNormalizado r = new RegistroNormalizado();
        r.setMoneda("Dólares");

        assertThat(service.monedaDe(r)).isEqualTo(ReglasMonedaService.Moneda.USD);
    }
}