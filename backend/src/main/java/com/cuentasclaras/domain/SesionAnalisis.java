package com.cuentasclaras.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Sesión efímera de análisis. Vive en memoria mientras el usuario está
 * trabajando. Se elimina automáticamente tras un periodo de inactividad.
 */
public class SesionAnalisis {

    private final String id;
    private final Instant creadaEn;
    private Instant ultimoAcceso;
    private final List<RegistroNormalizado> registros;
    private final List<VistaGenerada> vistas;
    private final ResultadoAnalisis resultado;

    public SesionAnalisis(String id,
            List<RegistroNormalizado> registros,
            List<VistaGenerada> vistas,
            ResultadoAnalisis resultado) {
        this.id = id;
        this.creadaEn = Instant.now();
        this.ultimoAcceso = this.creadaEn;
        this.registros = new ArrayList<>(registros);
        this.vistas = new ArrayList<>(vistas);
        this.resultado = resultado;
    }

    public void tocar() {
        this.ultimoAcceso = Instant.now();
    }

    public String getId() {
        return id;
    }

    public Instant getCreadaEn() {
        return creadaEn;
    }

    public Instant getUltimoAcceso() {
        return ultimoAcceso;
    }

    public List<RegistroNormalizado> getRegistros() {
        return registros;
    }

    public List<VistaGenerada> getVistas() {
        return vistas;
    }

    public ResultadoAnalisis getResultado() {
        return resultado;
    }
}