package com.cuentasclaras.service;

import com.cuentasclaras.domain.RegistroNormalizado;
import com.cuentasclaras.domain.ResultadoAnalisis;
import com.cuentasclaras.domain.SesionAnalisis;
import com.cuentasclaras.domain.VistaGenerada;
import jakarta.annotation.PreDestroy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestiona sesiones de análisis efímeras en memoria.
 *
 * <p>
 * Reglas:
 * </p>
 * <ul>
 * <li>Cada POST /api/analisis crea una nueva sesión.</li>
 * <li>Las sesiones se limpian automáticamente tras {@value #TTL_MINUTOS}
 * minutos
 * de inactividad.</li>
 * <li>Al reiniciar el backend, todas las sesiones se pierden. Es esperado.</li>
 * </ul>
 */
@Service
public class SesionService {

    private static final int TTL_MINUTOS = 30;

    private final Map<String, SesionAnalisis> sesiones = new ConcurrentHashMap<>();

    public SesionAnalisis crear(List<RegistroNormalizado> registros,
            List<VistaGenerada> vistas,
            ResultadoAnalisis resultado) {
        String id = UUID.randomUUID().toString();
        SesionAnalisis s = new SesionAnalisis(id, registros, vistas, resultado);
        sesiones.put(id, s);
        return s;
    }

    public Optional<SesionAnalisis> obtener(String id) {
        SesionAnalisis s = sesiones.get(id);
        if (s != null)
            s.tocar();
        return Optional.ofNullable(s);
    }

    /**
     * Limpieza periódica de sesiones inactivas. Se ejecuta cada 5 minutos.
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void limpiarSesionesInactivas() {
        Instant limite = Instant.now().minus(Duration.ofMinutes(TTL_MINUTOS));
        sesiones.entrySet().removeIf(e -> e.getValue().getUltimoAcceso().isBefore(limite));
    }

    @PreDestroy
    public void limpiarTodo() {
        sesiones.clear();
    }

    /** Para tests y debugging. */
    public int totalSesiones() {
        return sesiones.size();
    }
}