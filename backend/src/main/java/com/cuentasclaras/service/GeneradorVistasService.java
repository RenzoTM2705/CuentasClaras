package com.cuentasclaras.service;

import com.cuentasclaras.domain.RegistroNormalizado;
import com.cuentasclaras.domain.ResumenMonetario;
import com.cuentasclaras.domain.ResumenProveedor;
import com.cuentasclaras.domain.VistaCompleta;
import com.cuentasclaras.domain.VistaGenerada;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Genera las vistas a partir de los registros normalizados.
 *
 * <p>
 * Reglas de generación:
 * </p>
 * <ul>
 * <li><b>Un solo archivo, una sola unidad operativa</b> (o sin columna de
 * unidad):
 * una única vista con el nombre del archivo, sin importar el modo.</li>
 * <li><b>Un solo archivo, varias unidades operativas</b>:
 * <ul>
 * <li>{@code combinar} → una vista con el nombre del archivo.</li>
 * <li>{@code independiente} → una vista por unidad operativa.</li>
 * <li>{@code ambos} → una vista por unidad + una vista combinada.</li>
 * </ul>
 * </li>
 * <li><b>Varios archivos</b>:
 * <ul>
 * <li>{@code combinar} → una vista {@code Combinado}.</li>
 * <li>{@code independiente} → una vista por archivo.</li>
 * <li>{@code ambos} → una vista por archivo + una {@code Combinado}.</li>
 * </ul>
 * </li>
 * </ul>
 */
@Service
public class GeneradorVistasService {

    private static final int MAX_FILAS_PREVIEW = 8;
    private static final String NOMBRE_COMBINADO = "Combinado";

    private final ReglasMonedaService moneda;

    public GeneradorVistasService(ReglasMonedaService moneda) {
        this.moneda = moneda;
    }

    public List<VistaGenerada> generar(List<RegistroNormalizado> registros, String modo) {
        List<VistaGenerada> vistas = new ArrayList<>();

        LinkedHashSet<String> archivos = registros.stream()
                .map(RegistroNormalizado::getArchivoOrigen)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        LinkedHashSet<String> unidades = registros.stream()
                .map(RegistroNormalizado::getUnidadOperativa)
                .filter(u -> u != null && !u.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        boolean unSoloArchivo = archivos.size() <= 1;
        boolean variasUnidadesEnUnArchivo = unSoloArchivo && unidades.size() > 1;

        // Caso 1: un solo archivo con una sola unidad (o sin unidades)
        if (unSoloArchivo && !variasUnidadesEnUnArchivo) {
            String nombreArchivo = archivos.isEmpty() ? "Sin archivo" : archivos.iterator().next();
            vistas.add(construir("archivo:" + nombreArchivo, nombreArchivo, "archivo", registros));
            return vistas;
        }

        // Caso 2: un solo archivo con varias unidades operativas
        if (variasUnidadesEnUnArchivo) {
            boolean incluirUnidades = "independiente".equals(modo) || "ambos".equals(modo);
            boolean incluirCombinado = "ambos".equals(modo);

            if (incluirUnidades) {
                Map<String, List<RegistroNormalizado>> porUnidad = registros.stream()
                        .filter(r -> r.getUnidadOperativa() != null && !r.getUnidadOperativa().isBlank())
                        .collect(Collectors.groupingBy(
                                RegistroNormalizado::getUnidadOperativa,
                                LinkedHashMap::new,
                                Collectors.toList()));
                for (Map.Entry<String, List<RegistroNormalizado>> e : porUnidad.entrySet()) {
                    vistas.add(construir(
                            "unidad:" + e.getKey(),
                            "Unidad " + e.getKey(),
                            "unidad",
                            e.getValue()));
                }
            }

            // Modo combinar → 1 vista con el nombre del archivo
            // Modo ambos → 1 vista combinada adicional
            if ("combinar".equals(modo) || incluirCombinado) {
                String nombreArchivo = archivos.isEmpty() ? "Sin archivo" : archivos.iterator().next();
                vistas.add(construir(
                        "combinado",
                        nombreArchivo + " (combinado)",
                        "combinado",
                        registros));
            }

            if (vistas.isEmpty()) {
                String nombreArchivo = archivos.isEmpty() ? "Sin archivo" : archivos.iterator().next();
                vistas.add(construir("archivo:" + nombreArchivo, nombreArchivo, "archivo", registros));
            }

            return vistas;
        }

        // Caso 3: varios archivos
        boolean incluirIndividuales = "independiente".equals(modo) || "ambos".equals(modo);
        boolean incluirCombinado = "combinar".equals(modo) || "ambos".equals(modo);

        if (incluirIndividuales) {
            Map<String, List<RegistroNormalizado>> porArchivo = registros.stream()
                    .filter(r -> r.getArchivoOrigen() != null)
                    .collect(Collectors.groupingBy(
                            RegistroNormalizado::getArchivoOrigen,
                            LinkedHashMap::new,
                            Collectors.toList()));
            for (Map.Entry<String, List<RegistroNormalizado>> e : porArchivo.entrySet()) {
                vistas.add(construir("archivo:" + e.getKey(), e.getKey(), "archivo", e.getValue()));
            }
        }

        if (incluirCombinado) {
            vistas.add(construir("combinado", NOMBRE_COMBINADO, "combinado", registros));
        }

        if (vistas.isEmpty()) {
            vistas.add(construir("combinado", NOMBRE_COMBINADO, "combinado", registros));
        }

        return vistas;
    }

    public VistaCompleta construirCompleta(VistaGenerada catalogo,
            List<RegistroNormalizado> registrosSesion) {
        List<RegistroNormalizado> seleccionados = seleccionarRegistros(catalogo, registrosSesion);
        VistaCompleta v = new VistaCompleta();
        v.setId(catalogo.getId());
        v.setNombre(catalogo.getNombre());
        v.setTipo(catalogo.getTipo());
        v.setTotalDocumentos(seleccionados.size());
        v.setResumen(catalogo.getResumen());
        v.setResumenPorProveedor(catalogo.getResumenPorProveedor());
        v.setUnidadesOperativas(catalogo.getUnidadesOperativas());
        v.setColumnas(catalogo.getColumnas());
        v.setFilas(filasCompletas(seleccionados));
        return v;
    }

    /**
     * Selecciona los registros que corresponden a la vista del catálogo:
     * <ul>
     * <li>{@code combinado} → todos.</li>
     * <li>{@code archivo:nombre} → solo los del archivo.</li>
     * <li>{@code unidad:PE_LOG} → solo los de esa unidad operativa.</li>
     * </ul>
     */
    private List<RegistroNormalizado> seleccionarRegistros(VistaGenerada catalogo,
            List<RegistroNormalizado> registros) {
        if ("combinado".equals(catalogo.getTipo()))
            return registros;

        if ("archivo".equals(catalogo.getTipo())) {
            String nombreArchivo = catalogo.getId().startsWith("archivo:")
                    ? catalogo.getId().substring("archivo:".length())
                    : catalogo.getNombre();
            return registros.stream()
                    .filter(r -> nombreArchivo.equals(r.getArchivoOrigen()))
                    .toList();
        }

        if ("unidad".equals(catalogo.getTipo())) {
            String unidad = catalogo.getId().startsWith("unidad:")
                    ? catalogo.getId().substring("unidad:".length())
                    : catalogo.getNombre().replace("Unidad ", "");
            return registros.stream()
                    .filter(r -> unidad.equals(r.getUnidadOperativa()))
                    .toList();
        }

        return registros;
    }

    private VistaGenerada construir(String id, String nombre, String tipo,
            List<RegistroNormalizado> registros) {
        VistaGenerada v = new VistaGenerada();
        v.setId(id);
        v.setNombre(nombre);
        v.setTipo(tipo);
        v.setTotalDocumentos(registros.size());
        v.setResumen(construirResumen(registros));
        v.setResumenPorProveedor(construirResumenPorProveedor(registros));
        v.setUnidadesOperativas(extraerUnidadesOperativas(registros));
        v.setColumnas(columnasDe(registros));
        v.setFilasPreview(previewFilas(registros));
        return v;
    }

    private ResumenMonetario construirResumen(List<RegistroNormalizado> registros) {
        ResumenMonetario r = new ResumenMonetario();
        BigDecimal totalPEN = BigDecimal.ZERO;
        BigDecimal totalUSD = BigDecimal.ZERO;

        for (RegistroNormalizado reg : registros) {
            ReglasMonedaService.Moneda m = moneda.monedaDe(reg);
            BigDecimal saldoOrig = moneda.saldoOriginal(reg);

            if (m == ReglasMonedaService.Moneda.PEN && saldoOrig != null) {
                totalPEN = totalPEN.add(saldoOrig);
            } else if (m == ReglasMonedaService.Moneda.USD && saldoOrig != null) {
                totalUSD = totalUSD.add(saldoOrig);
            }
        }

        r.setTotalPEN(totalPEN);
        r.setTotalUSD(totalUSD);
        return r;
    }

    private List<ResumenProveedor> construirResumenPorProveedor(List<RegistroNormalizado> registros) {
        Map<String, ResumenProveedor> porProveedor = new LinkedHashMap<>();

        for (RegistroNormalizado r : registros) {
            String clave;
            if (r.getRuc() != null && !r.getRuc().isBlank()) {
                clave = "RUC:" + r.getRuc().trim();
            } else if (r.getProveedor() != null && !r.getProveedor().isBlank()) {
                clave = "NOMBRE:" + r.getProveedor().trim().toUpperCase();
            } else {
                clave = "SIN_PROVEEDOR";
            }

            ResumenProveedor rp = porProveedor.computeIfAbsent(clave, k -> {
                ResumenProveedor nuevo = new ResumenProveedor(r.getRuc(), r.getProveedor());
                if (nuevo.getProveedor() == null)
                    nuevo.setProveedor("(sin proveedor)");
                return nuevo;
            });

            rp.setTotalDocumentos(rp.getTotalDocumentos() + 1);

            ReglasMonedaService.Moneda m = moneda.monedaDe(r);
            BigDecimal saldoOrig = moneda.saldoOriginal(r);

            if (m == ReglasMonedaService.Moneda.PEN && saldoOrig != null) {
                rp.setTotalPEN(rp.getTotalPEN().add(saldoOrig));
            } else if (m == ReglasMonedaService.Moneda.USD && saldoOrig != null) {
                rp.setTotalUSD(rp.getTotalUSD().add(saldoOrig));
            }
        }

        List<ResumenProveedor> lista = new ArrayList<>(porProveedor.values());
        lista.sort((a, b) -> {
            int cmp = b.getTotalPEN().compareTo(a.getTotalPEN());
            if (cmp != 0)
                return cmp;
            return b.getTotalUSD().compareTo(a.getTotalUSD());
        });

        return lista;
    }

    private List<String> extraerUnidadesOperativas(List<RegistroNormalizado> registros) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (RegistroNormalizado r : registros) {
            String u = r.getUnidadOperativa();
            if (u != null && !u.isBlank())
                set.add(u.trim());
        }
        List<String> lista = new ArrayList<>(set);
        Collections.sort(lista);
        return lista;
    }

    private List<String> columnasDe(List<RegistroNormalizado> registros) {
        for (RegistroNormalizado r : registros) {
            if (r.getValoresOriginales() != null && !r.getValoresOriginales().isEmpty()) {
                return r.getValoresOriginales().keySet().stream()
                        .filter(k -> !"__filaOrigen".equals(k))
                        .toList();
            }
        }
        return List.of();
    }

    private List<List<Object>> previewFilas(List<RegistroNormalizado> registros) {
        List<List<Object>> filas = new ArrayList<>();
        int limite = Math.min(registros.size(), MAX_FILAS_PREVIEW);
        for (int i = 0; i < limite; i++)
            filas.add(filaDe(registros.get(i)));
        return filas;
    }

    private List<List<Object>> filasCompletas(List<RegistroNormalizado> registros) {
        List<List<Object>> filas = new ArrayList<>(registros.size());
        for (RegistroNormalizado r : registros)
            filas.add(filaDe(r));
        return filas;
    }

    private List<Object> filaDe(RegistroNormalizado r) {
        if (r.getValoresOriginales() == null)
            return List.of();
        List<Object> fila = new ArrayList<>();
        for (Map.Entry<String, Object> e : r.getValoresOriginales().entrySet()) {
            if ("__filaOrigen".equals(e.getKey()))
                continue;
            fila.add(e.getValue());
        }
        return fila;
    }
}