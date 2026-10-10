package com.cuentasclaras.importer;

import com.cuentasclaras.domain.Advertencia;
import com.cuentasclaras.domain.RegistroNormalizado;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Convierte una fila cruda de Excel a RegistroNormalizado.
 * Los valores originales se conservan para la capa de presentación.
 */
@Component
public class NormalizadorFilas {

    private final CatalogoAlias catalogo;

    public NormalizadorFilas(CatalogoAlias catalogo) {
        this.catalogo = catalogo;
    }

    public ResultadoNormalizacion normalizar(
            Map<String, Object> filaCruda,
            String archivoOrigen,
            String hojaOrigen) {
        RegistroNormalizado r = new RegistroNormalizado();
        r.setArchivoOrigen(archivoOrigen);
        r.setHojaOrigen(hojaOrigen);
        Object filaOrigen = filaCruda.get("__filaOrigen");
        r.setNumeroFilaOrigen(filaOrigen instanceof Number n ? n.intValue() : 0);
        r.setValoresOriginales(filaCruda);

        List<Advertencia> advertencias = new ArrayList<>();

        for (Map.Entry<String, Object> e : filaCruda.entrySet()) {
            String encabezado = e.getKey();
            if ("__filaOrigen".equals(encabezado))
                continue;

            String campo = catalogo.campoPara(encabezado);
            if (campo == null)
                continue;

            Object valor = e.getValue();
            if (valor == null || (valor instanceof String s && s.isBlank()))
                continue;

            try {
                asignarCampo(r, campo, valor);
            } catch (Exception ex) {
                Advertencia w = new Advertencia(
                        "VALOR_INVALIDO",
                        "No se pudo interpretar el valor '" + valor + "' para el campo '" + campo + "'");
                w.setArchivo(archivoOrigen);
                w.setHoja(hojaOrigen);
                w.setFila(r.getNumeroFilaOrigen());
                advertencias.add(w);
            }
        }

        return new ResultadoNormalizacion(r, advertencias);
    }

    private void asignarCampo(RegistroNormalizado r, String campo, Object valor) {
        switch (campo) {
            case "unidadOperativa" -> r.setUnidadOperativa(texto(valor));
            case "ruc" -> r.setRuc(texto(valor));
            case "proveedor" -> r.setProveedor(texto(valor));
            case "beneficiario" -> r.setBeneficiario(texto(valor));
            case "tipoDocumento" -> r.setTipoDocumento(texto(valor));
            case "categoria" -> r.setCategoria(texto(valor));
            case "numeroComprobante" -> r.setNumeroComprobante(texto(valor));
            case "fechaEmision" -> r.setFechaEmision(fecha(valor));
            case "fechaContable" -> r.setFechaContable(fecha(valor));
            case "fechaVencimiento" -> r.setFechaVencimiento(fecha(valor));
            case "moneda" -> r.setMoneda(texto(valor).toUpperCase());
            case "importeOriginal" -> r.setImporteOriginal(decimal(valor));
            case "tipoCambio" -> r.setTipoCambio(decimal(valor));
            case "saldoMonedaIngresada" -> r.setSaldoMonedaIngresada(decimal(valor));
            case "saldoFuncionalPEN" -> r.setSaldoFuncionalPEN(decimal(valor));
            case "saldoUSD" -> r.setSaldoUSD(decimal(valor));
            case "saldoPEN" -> r.setSaldoPEN(decimal(valor));
            case "terminoPago" -> r.setTerminoPago(texto(valor));
            case "descripcion" -> r.setDescripcion(texto(valor));
            case "metodoPago" -> r.setMetodoPago(texto(valor));
            case "grupoPago" -> r.setGrupoPago(texto(valor));
            case "voucher" -> r.setVoucher(texto(valor));
            case "diasVencimientoTexto" -> r.setDiasVencimientoTexto(texto(valor));
            case "cuentaAPagar" -> r.setCuentaAPagar(texto(valor));
            case "saldoFuncionalPENHistorico" -> r.setSaldoFuncionalPENHistorico(decimal(valor));
            case "fechaRecepcion" -> r.setFechaRecepcion(fecha(valor));
            case "numeroFacturaMaximo" -> r.setNumeroFacturaMaximo(texto(valor));
            default -> {
            }
        }
    }

    private String texto(Object v) {
        if (v == null)
            return null;
        if (v instanceof Number n) {
            double d = n.doubleValue();
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return String.valueOf((long) d);
            }
            return n.toString();
        }
        return v.toString().trim();
    }

    private BigDecimal decimal(Object v) {
        if (v == null)
            return null;
        if (v instanceof BigDecimal b)
            return b;
        if (v instanceof Number n)
            return BigDecimal.valueOf(n.doubleValue());
        String s = v.toString().trim().replace(",", "");
        if (s.isEmpty())
            return null;
        return new BigDecimal(s);
    }

    private LocalDate fecha(Object v) {
        if (v == null)
            return null;
        if (v instanceof LocalDate d)
            return d;
        if (v instanceof LocalDateTime dt)
            return dt.toLocalDate();
        if (v instanceof java.util.Date d)
            return d.toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        String s = v.toString().trim();
        if (s.isEmpty())
            return null;
        try {
            return LocalDate.parse(s);
        } catch (Exception ignored) {
        }
        try {
            String[] p = s.split("[/\\-]");
            if (p.length == 3) {
                int a, m, d;
                if (p[0].length() == 4) {
                    a = Integer.parseInt(p[0]);
                    m = Integer.parseInt(p[1]);
                    d = Integer.parseInt(p[2]);
                } else {
                    d = Integer.parseInt(p[0]);
                    m = Integer.parseInt(p[1]);
                    a = Integer.parseInt(p[2]);
                }
                return LocalDate.of(a, m, d);
            }
        } catch (Exception ignored) {
        }
        throw new IllegalArgumentException("Fecha no reconocida: " + s);
    }

    private Integer entero(Object v) {
        if (v == null)
            return null;
        if (v instanceof Number n)
            return n.intValue();
        String s = v.toString().trim();
        if (s.isEmpty())
            return null;
        return Integer.parseInt(s);
    }

    public record ResultadoNormalizacion(RegistroNormalizado registro, List<Advertencia> advertencias) {
    }
}