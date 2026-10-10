package com.cuentasclaras.service;

import com.cuentasclaras.domain.MarcaDuplicado;
import com.cuentasclaras.domain.RegistroNormalizado;
import com.cuentasclaras.domain.ResumenCombinacion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeduplicacionServiceTest {

    private final DeduplicacionService service = new DeduplicacionService();

    @Test
    void registrosUnicosNoSeMarcan() {
        RegistroNormalizado a = registro("C1", "100.00");
        RegistroNormalizado b = registro("C2", "200.00");

        ResumenCombinacion r = service.analizar(List.of(a, b), 1, List.of());

        assertThat(r.getRegistrosUnicos()).isEqualTo(2);
        assertThat(r.getDuplicadosExactos()).isZero();
        assertThat(r.getDuplicadosParciales()).isZero();
        assertThat(a.getMarcaDuplicado()).isNull();
        assertThat(b.getMarcaDuplicado()).isNull();
    }

    @Test
    void duplicadosExactosSeMarcan() {
        RegistroNormalizado a = registro("C1", "100.00");
        RegistroNormalizado b = registro("C1", "100.00");

        ResumenCombinacion r = service.analizar(List.of(a, b), 1, List.of());

        assertThat(r.getDuplicadosExactos()).isEqualTo(1);
        assertThat(a.getMarcaDuplicado()).isNotNull();
        assertThat(a.getMarcaDuplicado().getTipo()).isEqualTo(MarcaDuplicado.Tipo.EXACTO);
        assertThat(b.getMarcaDuplicado().getTipo()).isEqualTo(MarcaDuplicado.Tipo.EXACTO);
    }

    @Test
    void mismaClaveConImportesDistintosEsDuplicadoParcial() {
        RegistroNormalizado a = registro("C1", "100.00");
        RegistroNormalizado b = registro("C1", "999.00");

        ResumenCombinacion r = service.analizar(List.of(a, b), 1, List.of());

        assertThat(r.getDuplicadosParciales()).isEqualTo(2);
        assertThat(a.getMarcaDuplicado().getTipo()).isEqualTo(MarcaDuplicado.Tipo.PARCIAL);
        assertThat(b.getMarcaDuplicado().getTipo()).isEqualTo(MarcaDuplicado.Tipo.PARCIAL);
    }

    private RegistroNormalizado registro(String clave, String importe) {
        RegistroNormalizado r = new RegistroNormalizado();
        r.setClaveDocumento(clave);
        r.setImporteOriginal(new BigDecimal(importe));
        r.setMoneda("PEN");
        return r;
    }
}