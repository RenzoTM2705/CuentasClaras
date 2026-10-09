package com.cuentasclaras.service;

import com.cuentasclaras.domain.RegistroNormalizado;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Genera la clave de identificación de un documento para detectar duplicados.
 *
 * <p>
 * Clave principal (sección 7 de la especificación):
 * </p>
 * 
 * <pre>
 * unidad operativa + RUC + tipo de documento + número de comprobante
 * </pre>
 *
 * <p>
 * Si faltan campos esenciales, se genera una clave alternativa y el registro
 * se marca como ambiguo para revisión.
 * </p>
 *
 * <p>
 * Esta clase es el único punto donde se decide la clave. Cualquier cambio
 * futuro de la política de identificación se hace aquí.
 * </p>
 */
@Service
public class ClaveDocumentoService {

    /**
     * Genera la clave principal si hay suficientes campos. Si no, genera una
     * clave alternativa y devuelve {@code true} en {@code ambigua}.
     */
    public ClaveResultado generar(RegistroNormalizado r) {
        String unidad = normalizar(r.getUnidadOperativa());
        String ruc = normalizar(r.getRuc());
        String tipoDoc = normalizar(r.getTipoDocumento());
        String numero = normalizar(r.getNumeroComprobante());

        boolean completa = !unidad.isEmpty() && !ruc.isEmpty() && !tipoDoc.isEmpty() && !numero.isEmpty();

        String clave;
        boolean ambigua;

        if (completa) {
            clave = String.join("|", unidad, ruc, tipoDoc, numero);
            ambigua = false;
        } else {
            // Clave alternativa: intentar con lo que haya. Marcar como ambigua.
            clave = "ALT|"
                    + (unidad.isEmpty() ? "?" : unidad) + "|"
                    + (ruc.isEmpty() ? "?" : ruc) + "|"
                    + (tipoDoc.isEmpty() ? "?" : tipoDoc) + "|"
                    + (numero.isEmpty() ? "?" : numero) + "|"
                    + normalizar(r.getProveedor()) + "|"
                    + (r.getImporteOriginal() == null ? "?" : r.getImporteOriginal().toPlainString());
            ambigua = true;
        }

        r.setClaveDocumento(clave);
        return new ClaveResultado(clave, ambigua);
    }

    /**
     * Normaliza texto: minúsculas, sin tildes, sin espacios múltiples, sin
     * puntuación.
     * NO se usa para presentar al usuario: solo para comparar.
     */
    private String normalizar(String s) {
        if (s == null)
            return "";
        String sinTildes = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }

    public record ClaveResultado(String clave, boolean ambigua) {
    }
}