package com.cuentasclaras.dto;

import com.cuentasclaras.domain.Advertencia;
import com.cuentasclaras.domain.ResumenCombinacion;
import com.cuentasclaras.domain.VistaGenerada;

import java.util.List;

public class AnalisisResponse {

    private int totalFilasLeidas;
    private int totalRegistrosValidos;
    private int totalRegistrosRechazados;
    private List<VistaGenerada> vistas;
    private List<Advertencia> advertencias;
    private ResumenCombinacion resumenCombinacion;

    public AnalisisResponse() {
    }

    public int getTotalFilasLeidas() {
        return totalFilasLeidas;
    }

    public void setTotalFilasLeidas(int totalFilasLeidas) {
        this.totalFilasLeidas = totalFilasLeidas;
    }

    public int getTotalRegistrosValidos() {
        return totalRegistrosValidos;
    }

    public void setTotalRegistrosValidos(int totalRegistrosValidos) {
        this.totalRegistrosValidos = totalRegistrosValidos;
    }

    public int getTotalRegistrosRechazados() {
        return totalRegistrosRechazados;
    }

    public void setTotalRegistrosRechazados(int totalRegistrosRechazados) {
        this.totalRegistrosRechazados = totalRegistrosRechazados;
    }

    public List<VistaGenerada> getVistas() {
        return vistas;
    }

    public void setVistas(List<VistaGenerada> vistas) {
        this.vistas = vistas;
    }

    public List<Advertencia> getAdvertencias() {
        return advertencias;
    }

    public void setAdvertencias(List<Advertencia> advertencias) {
        this.advertencias = advertencias;
    }

    public ResumenCombinacion getResumenCombinacion() {
        return resumenCombinacion;
    }

    public void setResumenCombinacion(ResumenCombinacion r) {
        this.resumenCombinacion = r;
    }
}