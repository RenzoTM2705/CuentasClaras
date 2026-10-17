# Changelog

Todos los cambios notables de este proyecto se documentan en este archivo.

## [Fase 8] — Pruebas y despliegue

### Añadido
- Pruebas unitarias en backend (JUnit 5 + AssertJ):
  - Reglas de moneda: PEN/USD, equivalencia pendiente.
  - Clave de deduplicación: completa, incompleta, normalización.
  - Catálogo de alias: tildes, mayúsculas, espacios.
  - Deduplicación: exactos, parciales, únicos.
- Configuración de despliegue:
  - `frontend/vercel.json` para Vercel.
  - `backend/render.yaml` y `backend/Dockerfile` para Render.
  - `application-prod.yml` con `PORT` dinámico.
- Documentación:
  - `docs/despliegue.md` con guía paso a paso.
  - `README.md` actualizado con advertencia de seguridad.

### Desplegado
- Frontend en Vercel: `https://cuentas-claras-teal.vercel.app`
- Backend en Render: `https://cuentasclaras-backend.onrender.com`

### Notas operativas
- Backend en Render (Plan Free): CPU compartida, 512 MB de RAM y suspensión (sleep) tras 15 minutos de inactividad.
- Se configuró UptimeRobot para hacer ping al endpoint `/api/health` cada 5 minutos y mantener el backend despierto.
- Sin base de datos: los análisis son efímeros en memoria.
- Sin autenticación: se recomienda añadir una capa de seguridad si el despliegue queda público.

---

## [Fase 7.3.1] — Corrección de estilos en hoja Tratado

### Cambiado
- Se eliminaron los colores de fondo (azul, rojo, amarillo) de la hoja `Tratado` del Excel exportado.
- Todas las celdas de datos ahora tienen fondo blanco, manteniendo el formato original solicitado.
- Se conserva la fila `Total general` al final con la suma de todas las columnas.

---

## [Fase 7.3] — Hoja Tratado y filas null

### Añadido
- Nueva hoja `Tratado` en el Excel exportado con una tabla dinámica de proveedores por unidad operativa.
- Estructura de tabla dinámica:
  - Fila superior combinando los nombres de las unidades operativas.
  - Subencabezados de `Suma de Total USD` y `Suma de Total PEN` por unidad.
  - Columnas finales de totales generales.
  - Fila final `Total general` con la suma de cada columna.
- Orden alfabético para unidades operativas y proveedores.

### Cambiado
- Las filas con todos los valores nulos (`null`, `NULL`, `#N/A`, `N/A`, `-`) se descartan silenciosamente en la importación y la exportación.
- Aplica a las tres hojas: `Detalle`, `Resumen` y `Tratado`.

---

## [Fase 7.2] — Proveedores agrupados por unidad operativa

### Cambiado
- El bloque "Total de deuda por proveedor" en la hoja `Resumen` ahora es "Total de deuda por unidad operativa y proveedor".
- Nueva columna "Unidad operativa" al inicio de la tabla de proveedores.
- Un mismo proveedor puede aparecer en varias filas si tiene deuda en distintas unidades operativas.
- Ordenado por unidad operativa, y luego por Total PEN descendente.

---

## [Fase 7.1] — Total por unidad operativa

### Añadido
- Bloque "Total de deuda por unidad operativa" en la hoja `Resumen`.
- Columna "Docs" (documentos) eliminada de la tabla de proveedores.
- Los totales respetan los filtros aplicados en la interfaz.

---

## [Fase 7] — Exportación a Excel

### Añadido
- `ExportadorExcelService`: genera archivos `.xlsx` con dos hojas (`Detalle` y `Resumen`).
- Endpoint: `POST /api/sesiones/{sessionId}/vistas/{vistaId}/exportar`.
- Respeto de los filtros aplicados en la UI.
- PEN y USD siempre separados.
- Clasificación automática del tipo de columna (MONEDA / FECHA / TEXTO) para aplicar formatos correctos en Excel.
- Fechas como tipo fecha real y números con formato `#,##0.00`.
- Identificadores como RUC, voucher, cuenta bancaria se exportan como texto plano.

---

## [Fase 6] — Análisis financiero

### Añadido
- Filtros en la vista ampliada: por proveedor/RUC, unidad operativa y moneda.
- Ordenamiento por columna (ascendente / descendente / sin orden).
- Totales al pie que respetan los filtros, con PEN y USD separados.
- Resumen por proveedor en la vista ampliada.

### Decisiones
- Análisis exclusivamente monetario (no se calculan vencimientos ni fechas de corte).
- Si se sube 1 archivo con N unidades operativas en modo "independiente", se genera una vista por unidad.
- Si se sube 1 archivo en modo "ambos", se genera una vista por unidad + una combinada.

---

## [Fase 5] — Selector visual de vistas

### Añadido
- Pantalla inicial que sube archivos reales al backend (`POST /api/analisis`).
- Mosaico tipo *Windows + Tab* que se adapta a 1, 2, 3, 4 o más vistas.
- Región de vista con icono Excel, nombre y subtítulo de identificadores.
- Vista ampliada a pantalla completa con cierre por botón o `Esc`.
- Resumen general por vista, con PEN y USD siempre separados.
- Tabla de detalle con encabezados originales y valores sin transformar.
- Animación breve de reparto de cartas al primer render del mosaico.
- Navegación lineal sin router: subir → ver → ampliar → volver → subir.

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
- El cálculo real de vencimientos se pospone (finalmente descartado en la Fase 6).

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