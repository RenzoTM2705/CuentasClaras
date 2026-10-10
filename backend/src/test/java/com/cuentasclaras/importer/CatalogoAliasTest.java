package com.cuentasclaras.importer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogoAliasTest {

    private final CatalogoAlias catalogo = new CatalogoAlias();

    @Test
    void reconoceEncabezadosConMayusculasYTildes() {
        assertThat(catalogo.campoPara("PROVEEDOR")).isEqualTo("proveedor");
        assertThat(catalogo.campoPara("RUC")).isEqualTo("ruc");
        assertThat(catalogo.campoPara("FECHA DE EMISIÓN")).isEqualTo("fechaEmision");
        assertThat(catalogo.campoPara("TÉRMINO DE PAGO")).isEqualTo("terminoPago");
        assertThat(catalogo.campoPara("SALDO EN MONEDA FUNCIONAL PEN")).isEqualTo("saldoFuncionalPEN");
    }

    @Test
    void reconoceEncabezadosConEspaciosExtra() {
        assertThat(catalogo.campoPara("  Proveedor  ")).isEqualTo("proveedor");
        assertThat(catalogo.campoPara("Número   de   Documento")).isEqualTo("numeroComprobante");
    }

    @Test
    void encabezadoDesconocidoDevuelveNull() {
        assertThat(catalogo.campoPara("COLUMNA_RARA_QUE_NO_EXISTE")).isNull();
        assertThat(catalogo.campoPara("")).isNull();
        assertThat(catalogo.campoPara(null)).isNull();
    }

    @Test
    void diasAVencerSeReconoceComoTextoNoComoNumero() {
        // En el catálogo actual, "dias a vencer" está registrado como
        // diasVencimientoTexto
        String campo = catalogo.campoPara("DIAS A VENCER");
        assertThat(campo).isEqualTo("diasVencimientoTexto");
    }
}