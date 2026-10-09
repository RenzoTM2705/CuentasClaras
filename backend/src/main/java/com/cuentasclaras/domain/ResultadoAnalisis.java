package com.cuentasclaras.domain;

import java.util.ArrayList;
import java.util.List;

public class ResultadoAnalisis {

    private List<RegistroNormalizado> registros = new ArrayList<>();
    private List<VistaGenerada> vistas = new ArrayList<>();
    private List<Advertencia> advertencias = new ArrayList<>();

    private int totalFilasLeidas;
    private int totalRegistrosValidos;
    private int totalRegistrosRechazados;

    public ResultadoAnalisis() {
    }

    public List<RegistroNormalizado> getRegistros() {
        return registros;
    }

    public void setRegistros(List<RegistroNormalizado> registros) {
        this.registros = registros;
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
}