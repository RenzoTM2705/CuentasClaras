# Changelog

## [Fase 8] — Pruebas y despliegue

### Añadido
- Pruebas unitarias en backend (JUnit 5 + AssertJ):
  - Reglas de moneda: PEN/USD, equivalencia pendiente.
  - Clave de deduplicación: completa, incompleta, normalización.
  - Catálogo de alias: tildes, mayúsculas, espacios.
  - Deduplicación: exactos, parciales, únicos.
- Configuración de despliegue:
  - `frontend/vercel.json` para Vercel.
  - `backend/render.yaml` para Render.
  - `application-prod.yml` con `PORT` dinámico.
- Documentación:
  - `docs/despliegue.md` con guía paso a paso.
  - `README.md` actualizado con advertencia de seguridad.

### Notas
- Sin base de datos, sin autenticación.
- El backend en Render Free tarda ~30 s en despertar tras inactividad.

## [Fase 5] — Selector visual de vistas

### Añadido
- Pantalla inicial que sube archivos reales al backend (`POST /api/analisis`).
- Mosaico tipo *Windows + Tab* que se adapta a 1, 2, 3, 4 o más vistas.
- Región de vista con icono Excel, nombre y subtítulo de identificadores.
- Vista ampliada a pantalla completa con cierre por botón o `Esc`.
- Resumen general por vista, con PEN y USD siempre separados.
- Resumen por proveedor dentro de la vista ampliada.
- Tabla de detalle con encabezados originales y valores sin transformar.
- Animación breve de reparto de cartas al primer render del mosaico.
- Navegación lineal sin router: subir → ver → ampliar → volver → subir.

### Cambiado
- La lógica de generación de vistas se simplifica:
  - 1 archivo → 1 vista con el nombre del archivo.
  - N archivos · `combinar` → 1 vista `Combinado`.
  - N archivos · `independiente` → N vistas por archivo.
  - N archivos · `ambos` → N vistas por archivo + 1 vista `Combinado`.
- Ya no se generan vistas por unidad operativa. Las unidades siguen apareciendo como columna en la tabla.

### Arquitectura
- **Sesiones efímeras en memoria**:
  - `POST /api/analisis` devuelve `sessionId` + catálogo ligero de vistas.
  - Cada vista incluye solo una preview de 8 filas para el mosaico.
  - `GET /api/sesiones/{sessionId}/vistas/{vistaId}` sirve la vista completa bajo demanda.
  - Las sesiones se limpian automáticamente tras 30 minutos sin uso.
  - Al reiniciar el backend, todas las sesiones se pierden (comportamiento esperado).

---

## [Fase 4] — Combinación avanzada y deduplicación

### Añadido
- `DeduplicacionService`: distingue duplicados **exactos** de **parciales**.
- `MarcaDuplicado`: marca registros sin eliminarlos.
- `ResumenCombinacion`: contadores de duplicados y advertencias estructurales.
- Detección de incompatibilidad entre archivos combinados.
- El endpoint `POST /api/analisis` devuelve `resumenCombinacion`.

### Reglas
- Los duplicados nunca se eliminan automáticamente.
- Los duplicados parciales se marcan para revisión.

---

## [Fase 3] — Importación inteligente de Excel

### Añadido
- Apache POI para leer `.xlsx` y `.xls`.
- `CatalogoAlias` ampliado con encabezados reales (sección 5.4 de la especificación).
- `DetectorEncabezados` sin posición fija, con nivel de confianza (ALTA / MEDIA / BAJA).
- Tolerancia a columnas no reconocidas: se conservan en `valoresOriginales`.
- `ReglasMonedaService`: PEN y USD nunca sumados, sin conversiones inventadas.
- `GeneradorVistasService`: genera vistas de archivo y combinado.
- Endpoint `POST /api/analisis` con subida multipart.
- Todo el procesamiento en memoria, sin persistencia.

### Cambiado
- La columna `DIAS A VENCER` se trata como **texto descriptivo**, no como número.
- El cálculo real de vencimientos se pospone a la Fase 6.

---

## [Fase 2] — Modelo en memoria y contrato de API

### Añadido
- Modelo de dominio en memoria: `RegistroNormalizado`, `VistaGenerada`, `ResumenMonetario`, `Advertencia`, `ResultadoAnalisis`.
- `ClaveDocumentoService` centralizado para deduplicación.
- DTOs `AnalisisRequest` y `AnalisisResponse`.
- Endpoint provisional `POST /api/analisis` con datos simulados.

### Decisión
- **Sin base de datos.** El análisis es efímero en memoria, sin persistencia de Excel ni de resultados.
- Esto simplifica el despliegue y elimina el riesgo de almacenar datos financieros.

---

## [Fase 1] — Fundación

### Añadido
- Estructura del repositorio (frontend, backend, docs, CI).
- React + TypeScript + Vite + Tailwind con **pnpm**.
- Spring Boot con CORS local y endpoint `/api/health`.
- Pantalla inicial con zona de carga de archivos.
- Documentación inicial: decisiones y catálogo de campos.
- Workflow de CI básico.
- Flujo de ramas `main` / `develop` / `feature/*`.
