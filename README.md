# CuentasClaras

Aplicación web para **visualizar y consolidar** reportes Excel de cuentas por pagar.

Es una herramienta **exclusivamente visual y expositiva**. No edita documentos financieros, no gestiona pagos, no tiene flujos de aprobación ni login tradicional.

## Características

- Carga múltiple de archivos Excel (`.xlsx`, `.xls`).
- Detección inteligente de encabezados, sin depender de posiciones fijas.
- Normalización de columnas mediante catálogo de alias.
- Combinación de archivos compatibles sin duplicar registros.
- Mosaico visual tipo *Windows + Tab* con previsualizaciones.
- Vista ampliada a pantalla completa con tabla completa y resumen financiero.
- Resumen general y resumen por proveedor, siempre con **PEN y USD separados**.
- Sesiones efímeras en memoria: no se guarda nada en base de datos.
- Sin login: pensado para uso individual.

## Estado actual

| Fase | Estado |
|---|---|
| Fase 1 — Fundación | ✅ |
| Fase 2 — Modelo en memoria y contrato API | ✅ |
| Fase 3 — Importación inteligente de Excel | ✅ |
| Fase 4 — Combinación y deduplicación | ✅ |
| Fase 5 — Selector visual de vistas | ✅ |
| Fase 6 — Análisis financiero | ⏳ pendiente |
| Fase 7 — Exportación | ⏳ pendiente |
| Fase 8 — Pruebas y despliegue | ⏳ pendiente |

Ver `CHANGELOG.md` para el detalle por fase.

## Estructura del repositorio
