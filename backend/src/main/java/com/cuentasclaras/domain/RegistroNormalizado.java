package com.cuentasclaras.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Un registro individual proveniente de un Excel, normalizado a campos
 * internos.
 *
 * <p>
 * Importante: la normalización es solo interna. Los valores originales de la
 * fila
 * se conservan en {@link #valoresOriginales} para que la capa de presentación
 * pueda
 * mostrar el Excel tal como venía.
 * </p>
 */
public class RegistroNormalizado {

    // Identificación del origen
    private String archivoOrigen;
    private String hojaOrigen;
    private int numeroFilaOrigen;

    // Campos de la sección 5.4 (todos opcionales)
    private String unidadOperativa;
    private String ruc;
    private String proveedor;
    private String beneficiario;
    private String tipoDocumento;
    private String categoria;
    private String numeroComprobante;
    private LocalDate fechaEmision;
    private LocalDate fechaContable;
    private String moneda;
    private BigDecimal importeOriginal;
    private BigDecimal tipoCambio;
    private BigDecimal saldoMonedaIngresada;
    private BigDecimal saldoFuncionalPEN;
    private BigDecimal saldoUSD;
    private BigDecimal saldoPEN;
    private LocalDate fechaVencimiento;
    private String terminoPago;
    private String descripcion;
    private String metodoPago;
    private String grupoPago;
    private String voucher;
    private Integer diasVencimiento;

    // Clave calculada para deduplicación
    private String claveDocumento;

    // Valores originales de la fila, tal como venían en el Excel
    private Map<String, Object> valoresOriginales;

    // Constructor vacío + getters/setters
    public RegistroNormalizado() {
    }

    public String getArchivoOrigen() {
        return archivoOrigen;
    }

    public void setArchivoOrigen(String archivoOrigen) {
        this.archivoOrigen = archivoOrigen;
    }

    public String getHojaOrigen() {
        return hojaOrigen;
    }

    public void setHojaOrigen(String hojaOrigen) {
        this.hojaOrigen = hojaOrigen;
    }

    public int getNumeroFilaOrigen() {
        return numeroFilaOrigen;
    }

    public void setNumeroFilaOrigen(int numeroFilaOrigen) {
        this.numeroFilaOrigen = numeroFilaOrigen;
    }

    public String getUnidadOperativa() {
        return unidadOperativa;
    }

    public void setUnidadOperativa(String unidadOperativa) {
        this.unidadOperativa = unidadOperativa;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public String getBeneficiario() {
        return beneficiario;
    }

    public void setBeneficiario(String beneficiario) {
        this.beneficiario = beneficiario;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public void setNumeroComprobante(String numeroComprobante) {
        this.numeroComprobante = numeroComprobante;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public LocalDate getFechaContable() {
        return fechaContable;
    }

    public void setFechaContable(LocalDate fechaContable) {
        this.fechaContable = fechaContable;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public BigDecimal getImporteOriginal() {
        return importeOriginal;
    }

    public void setImporteOriginal(BigDecimal importeOriginal) {
        this.importeOriginal = importeOriginal;
    }

    public BigDecimal getTipoCambio() {
        return tipoCambio;
    }

    public void setTipoCambio(BigDecimal tipoCambio) {
        this.tipoCambio = tipoCambio;
    }

    public BigDecimal getSaldoMonedaIngresada() {
        return saldoMonedaIngresada;
    }

    public void setSaldoMonedaIngresada(BigDecimal saldoMonedaIngresada) {
        this.saldoMonedaIngresada = saldoMonedaIngresada;
    }

    public BigDecimal getSaldoFuncionalPEN() {
        return saldoFuncionalPEN;
    }

    public void setSaldoFuncionalPEN(BigDecimal saldoFuncionalPEN) {
        this.saldoFuncionalPEN = saldoFuncionalPEN;
    }

    public BigDecimal getSaldoUSD() {
        return saldoUSD;
    }

    public void setSaldoUSD(BigDecimal saldoUSD) {
        this.saldoUSD = saldoUSD;
    }

    public BigDecimal getSaldoPEN() {
        return saldoPEN;
    }

    public void setSaldoPEN(BigDecimal saldoPEN) {
        this.saldoPEN = saldoPEN;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getTerminoPago() {
        return terminoPago;
    }

    public void setTerminoPago(String terminoPago) {
        this.terminoPago = terminoPago;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getGrupoPago() {
        return grupoPago;
    }

    public void setGrupoPago(String grupoPago) {
        this.grupoPago = grupoPago;
    }

    public String getVoucher() {
        return voucher;
    }

    public void setVoucher(String voucher) {
        this.voucher = voucher;
    }

    public Integer getDiasVencimiento() {
        return diasVencimiento;
    }

    public void setDiasVencimiento(Integer diasVencimiento) {
        this.diasVencimiento = diasVencimiento;
    }

    public String getClaveDocumento() {
        return claveDocumento;
    }

    public void setClaveDocumento(String claveDocumento) {
        this.claveDocumento = claveDocumento;
    }

    public Map<String, Object> getValoresOriginales() {
        return valoresOriginales;
    }

    public void setValoresOriginales(Map<String, Object> valoresOriginales) {
        this.valoresOriginales = valoresOriginales;
    }
}