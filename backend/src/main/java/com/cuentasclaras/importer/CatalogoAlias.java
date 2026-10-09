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
        registrar("unidadOperativa", "unidad", "unidad operativa", "uo", "business unit");

        registrar("ruc", "ruc", "ruc proveedor", "tax id", "ruc del proveedor");
        registrar("proveedor", "proveedor", "nombre del proveedor", "supplier", "razon social");
        registrar("beneficiario", "beneficiario", "beneficiary");

        registrar("tipoDocumento", "tipo doc", "tipo documento", "document type", "tipo de documento");
        registrar("categoria", "categoria", "category");
        registrar("numeroComprobante", "numero", "numero comprobante", "comprobante",
                "nro doc", "nro documento", "invoice number", "documento");

        registrar("fechaEmision", "fecha emision", "fecha doc", "fecha documento", "issue date");
        registrar("fechaContable", "fecha contable", "accounting date");
        registrar("fechaVencimiento", "fecha vencimiento", "fecha venc", "due date", "vencimiento");

        registrar("moneda", "moneda", "currency", "mon");
        registrar("importeOriginal", "importe", "importe original", "amount", "monto");
        registrar("tipoCambio", "tipo cambio", "tc", "exchange rate", "tipo de cambio");
        registrar("saldoMonedaIngresada", "saldo moneda ingresada", "saldo ingresado",
                "saldo moneda", "saldo en moneda ingresada");
        registrar("saldoFuncionalPEN", "saldo funcional", "saldo funcional pen",
                "saldo pen funcional", "saldo funcional soles");
        registrar("saldoUSD", "saldo usd", "saldo dolares", "saldo $", "saldo en dolares");
        registrar("saldoPEN", "saldo pen", "saldo soles", "saldo s/", "saldo en soles");

        registrar("terminoPago", "termino pago", "condiciones pago", "condicion pago");
        registrar("descripcion", "descripcion", "detalle", "description", "glosa");
        registrar("metodoPago", "metodo pago", "forma pago", "forma de pago");
        registrar("grupoPago", "grupo pago", "payment group");
        registrar("voucher", "voucher", "comprobante pago", "numero voucher");
        registrar("diasVencimiento", "dias vencimiento", "dias vencido", "dias de vencimiento",
                "days overdue");
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