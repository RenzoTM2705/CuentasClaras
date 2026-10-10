package com.cuentasclaras.service;

import com.cuentasclaras.domain.RegistroNormalizado;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClaveDocumentoServiceTest {

    private final ClaveDocumentoService service = new ClaveDocumentoService();

    @Test
    void claveCompletaUsaCuatroCampos() {
        RegistroNormalizado r = new RegistroNormalizado();
        r.setUnidadOperativa("PE_LOG");
        r.setRuc("20123456789");
        r.setTipoDocumento("FACTURA");
        r.setNumeroComprobante("F001-123");

        ClaveDocumentoService.ClaveResultado resultado = service.generar(r);

        assertThat(resultado.ambigua()).isFalse();
        assertThat(r.getClaveDocumento()).contains("pe log");
        assertThat(r.getClaveDocumento()).contains("20123456789");
        assertThat(r.getClaveDocumento()).contains("factura");
        assertThat(r.getClaveDocumento()).contains("f001 123");
    }

    @Test
    void claveIncompletaMarcaComoAmbigua() {
        RegistroNormalizado r = new RegistroNormalizado();
        r.setUnidadOperativa("PE_LOG");
        // Falta RUC
        r.setTipoDocumento("FACTURA");
        r.setNumeroComprobante("F001-123");

        ClaveDocumentoService.ClaveResultado resultado = service.generar(r);

        assertThat(resultado.ambigua()).isTrue();
        assertThat(r.getClaveDocumento()).startsWith("ALT|");
    }

    @Test
    void dosRegistrosIgualesGeneranLaMismaClave() {
        RegistroNormalizado a = new RegistroNormalizado();
        a.setUnidadOperativa("PE_LOG");
        a.setRuc("20123456789");
        a.setTipoDocumento("FACTURA");
        a.setNumeroComprobante("F001-123");

        RegistroNormalizado b = new RegistroNormalizado();
        b.setUnidadOperativa("pe log"); // distinta caja
        b.setRuc(" 20123456789 "); // espacios
        b.setTipoDocumento("factura"); // minúsculas
        b.setNumeroComprobante("F001 123"); // espacio en vez de guion

        service.generar(a);
        service.generar(b);

        assertThat(a.getClaveDocumento()).isEqualTo(b.getClaveDocumento());
    }
}