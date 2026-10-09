# Changelog

## [Fase 1] — Fundación

- Estructura del repositorio
- React + TypeScript + Vite + Tailwind
- Spring Boot con CORS local
- Página inicial con zona de carga
- Endpoint /api/health

- ## [Fase 2] — Modelo y contrato de API

### Añadido
- Modelo de dominio en memoria: RegistroNormalizado, VistaGenerada,
  ResumenMonetario, Advertencia, ResultadoAnalisis
- ClaveDocumentoService centralizado para deduplicación
- DTOs AnalisisRequest y AnalisisResponse
- Endpoint provisional POST /api/analisis

### Decisión
- Sin base de datos: análisis efímero en memoria, sin persistencia
  de Excel ni de resultados. Simplifica despliegue y elimina riesgo
  de almacenar datos financieros.
