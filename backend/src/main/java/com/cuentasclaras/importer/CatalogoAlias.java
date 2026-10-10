package com.cuentasclaras.importer;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Catálogo centralizado de alias de encabezados.
 * La detección se basa en el encabezado normalizado, no en la posición
 * de la columna ni en el nombre del archivo.
 */
@Component
public class CatalogoAlias {

    private final Map<String, String> aliasACampo = new HashMap<>();

public CatalogoAlias() {
    // Unidad operativa
    registrar("unidadOperativa", "unidad", "unidad operativa", "uo", "business unit");

    // Proveedor
    registrar("ruc", "ruc", "ruc proveedor", "tax id", "ruc del proveedor");
    registrar("proveedor", "proveedor", "nombre del proveedor", "supplier", "razon social");
    registrar("beneficiario", "beneficiario", "beneficiary");

    // Documento
    registrar("tipoDocumento", "tipo doc", "tipo documento", "tipo de documento",
            "document type", "tipo doc.");
    registrar("categoria", "categoria", "category");
    registrar("numeroComprobante", "numero", "numero comprobante", "numero de documento",
                    "numero documento", "comprobante", "comprobante documento",
                    "nro doc", "nro documento", "nro de documento",
                    "invoice number", "documento");

    // Fechas
    registrar("fechaEmision", "fecha emision", "fecha de emision",
            "fecha doc", "fecha documento", "issue date");
    registrar("fechaContable", "fecha contable", "accounting date");
    registrar("fechaVencimiento", "fecha vencimiento", "fecha de vencimiento",
            "fecha venc", "due date", "vencimiento");

    // Moneda e importes
    registrar("moneda", "moneda", "divisa", "currency", "mon");
    registrar("importeOriginal", "importe", "importe original",
            "importe moneda ingresada", "amount", "monto", "importe ingresado");
    registrar("tipoCambio", "tipo cambio", "tipo de cambio", "tc", "exchange rate");
    registrar("saldoMonedaIngresada", "saldo moneda ingresada", "saldo ingresado",
            "saldo moneda", "saldo en moneda ingresada");
    registrar("saldoFuncionalPEN", "saldo funcional", "saldo funcional pen",
            "saldo en moneda funcional pen", "saldo pen funcional",
            "saldo funcional soles");
    registrar("saldoUSD", "saldo usd", "saldo dolares", "saldo $",
            "saldo en dolares", "saldo moneda usd");
    registrar("saldoPEN", "saldo pen", "saldo soles", "saldo s/",
            "saldo en soles", "saldo moneda pen");

    // Cuenta a pagar (financiero)
    registrar("cuentaAPagar", "cuenta a pagar", "cuenta por pagar",
            "cxp", "cuenta pago");

    // Histórico: campo distinto, para NO confundir con el funcional actual
    registrar("saldoFuncionalPENHistorico",
            "saldo moneda funcional pen historico",
            "saldo funcional pen historico",
            "saldo funcional historico");

    // Otros
    registrar("terminoPago", "termino pago", "termino de pago",
            "condiciones pago", "condicion pago", "condiciones de pago");
    registrar("descripcion", "descripcion", "descripcion de pago",
            "detalle", "description", "glosa");
    registrar("metodoPago", "metodo pago", "metodo de pago",
            "forma pago", "forma de pago");
    registrar("grupoPago", "grupo pago", "grupo de pago", "payment group");
    registrar("voucher", "voucher", "comprobante pago", "numero voucher");
    registrar("diasVencimientoTexto", "dias vencimiento", "dias vencido", "dias de vencimiento", "dias a vencer",
                    "days overdue");

    // Fechas auxiliares
    registrar("fechaRecepcion", "fecha recepcion", "fecha de recepcion",
            "fecha recepcion del documento", "fecha de recepcion del documento");

    // Número factura máximo (puede servir para análisis)
    registrar("numeroFacturaMaximo", "numero factura maximo",
            "numero factura max", "nro factura maximo");
}

    private void registrar(String campo, String... alias) {
        for (String a : alias) {
            aliasACampo.put(normalizar(a), campo);
        }
    }

    public String campoPara(String encabezado) {
        if (encabezado == null)
            return null;
        return aliasACampo.get(normalizar(encabezado));
    }

    public static String normalizar(String s) {
        if (s == null)
            return "";
        String sinTildes = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }

    public List<String> camposConocidos() {
        return aliasACampo.values().stream().distinct().sorted().toList();
    }
}