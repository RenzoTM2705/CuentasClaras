# Changelog

Todos los cambios notables de este proyecto se documentan en este archivo.

## [Fase 8] - Cruce de Proveedores y Comparativa por Fechas

### Añadido
- Hoja "Cruce Proveedores": Generación automática de la tabla de conciliación entre la plantilla maestra de TI y los datos de CxP.
  - Nueva lógica de semáforo que pinta solo el caracter sin fondo, copiando el color exacto de la plantilla.
  - Extracción automática de la fecha del reporte (buscando el texto "AL." en las primeras filas).
  - Agrupación de Términos de Pago y conteo de documentos.
- Comparativa por Fechas en Resumen: Tabla integrada a la derecha de los totales por proveedor.
  - Estructura de doble encabezado (Fechas en la fila superior, PEN/USD en la inferior).
  - Columna de "Diferencia" que calcula el delta entre la fecha más reciente y la más antigua.
  - Colores claros (Azul pálido, Amarillo claro, Lavanda) para identificar visualmente las fechas.
- Filtros de Limpieza: El sistema ahora ignora y descarta registros "(sin unidad)" y "(sin proveedor)" para evitar ensuciar las tablas exportadas.

### Cambiado
- El ExportadorExcelService ahora recibe la lista completa de RegistroNormalizado para poder generar las comparativas.
- El campo fechaReporte se añade al modelo de datos al momento de la importación.

---

## [Fase 7] - Exportación a Excel

### Añadido
- ExportadorExcelService: genera archivos .xlsx con tres hojas (Detalle, Resumen y Tratado).
- Endpoint: POST /api/sesiones/{sessionId}/vistas/{vistaId}/exportar.
- Respeto de los filtros aplicados en la UI.
- Clasificación automática del tipo de columna (MONEDA / FECHA / TEXTO) para aplicar formatos correctos en Excel.
- Hoja "Tratado": Tabla dinámica con columnas dobles (USD y PEN) por unidad operativa y totales generales.

---

## [Fase 6] - Análisis financiero

### Añadido
- Filtros en la vista ampliada: por proveedor/RUC, unidad operativa y moneda.
- Ordenamiento por columna (ascendente / descendente / sin orden).
- Totales al pie que respetan los filtros, con PEN y USD separados.
- Resumen por proveedor en la vista ampliada.

---

## [Fase 5] - Selector visual de vistas

### Añadido
- Mosaico tipo Windows + Tab que se adapta a 1, 2, 3, 4 o más vistas.
- Vista ampliada a pantalla completa con cierre por botón o Esc.
- Sesiones efímeras en memoria con TTL de 30 minutos.
- Animación breve de reparto de cartas al primer render del mosaico.

---

## [Fase 4] - Combinación avanzada y deduplicación

### Añadido
- DeduplicacionService: distingue duplicados exactos de parciales.
- MarcaDuplicado: marca registros sin eliminarlos.
- Detección de incompatibilidad entre archivos combinados.

---

## [Fase 3] - Importación inteligente de Excel

### Añadido
- Apache POI para leer .xlsx y .xls.
- DetectorEncabezados sin posición fija, con nivel de confianza.
- ReglasMonedaService: PEN y USD nunca sumados.
- La columna DIAS A VENCER se trata como texto descriptivo.

---

## [Fase 2] - Modelo en memoria y contrato de API

### Añadido
- Modelo de dominio en memoria: RegistroNormalizado, VistaGenerada, ResumenMonetario.
- ClaveDocumentoService centralizado para deduplicación.
- Decisión de arquitectura: Sin base de datos.

---

## [Fase 1] - Fundación

### Añadido
- Estructura del repositorio (frontend, backend, docs, CI).
- React + TypeScript + Vite + Tailwind con pnpm.
- Spring Boot con CORS local y endpoint /api/health.
