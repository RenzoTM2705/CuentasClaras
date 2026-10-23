# CuentasClaras

Aplicación web para visualizar, consolidar y analizar reportes Excel de cuentas por pagar.

Es una herramienta exclusivamente visual y expositiva. No edita documentos financieros, no gestiona pagos, no tiene flujos de aprobación ni login tradicional.

## Características

- Carga múltiple de archivos Excel (.xlsx, .xls).
- Detección inteligente de encabezados, sin depender de posiciones fijas.
- Normalización de columnas mediante catálogo de alias.
- Combinación de archivos compatibles sin duplicar registros.
- Mosaico visual tipo Windows + Tab con previsualizaciones.
- Vista ampliada a pantalla completa con tabla completa.
- Filtros por proveedor/RUC, unidad operativa y moneda.
- Ordenamiento por columna.
- Totales al pie que respetan los filtros, siempre con PEN y USD separados.
- Exportación a Excel (.xlsx) con cuatro hojas de análisis:
  - Detalle: tabla original con los datos.
  - Resumen: totales por unidad operativa, por proveedor y comparativa por fechas.
  - Tratado: tabla dinámica de proveedor x unidad operativa.
  - Cruce Proveedores: validación contra plantilla maestra de TI (con semáforos).
- Sesiones efímeras en memoria: no se guarda nada en base de datos.
- Sin login: pensado para uso individual.

## Arquitectura

- Frontend: React 18 + TypeScript + Vite + Tailwind CSS con pnpm.
- Backend: Java 17 + Spring Boot 3 + Apache POI.
- Base de datos: ninguna. Todo es efímero en memoria.

## Requisitos locales

- Node.js 20+
- pnpm 9+
- Java 17+
- Maven 3.9+

## Ejecución local

### Backend

cd backend
mvn spring-boot:run

Disponible en http://localhost:8080.

### Frontend

cd frontend
pnpm install
pnpm dev

Disponible en http://localhost:5173.

## Pruebas

cd backend
mvn test

Cubren:
- Reglas de moneda (PEN/USD, equivalencia pendiente).
- Clave de deduplicación.
- Catálogo de alias.
- Deduplicación exacta y parcial.

## Reglas financieras

- PEN y USD nunca se suman juntos.
- No se inventan tipos de cambio.
- PEN con columnas USD vacías se procesa normal.
- USD sin saldo funcional PEN queda con equivalencia pendiente.

## Advertencia de seguridad

Este proyecto no incluye autenticación. Si se despliega en una URL pública:
- Cualquier persona con la URL puede subir archivos y ver los datos analizados.
- Las sesiones se guardan en memoria del backend y se limpian tras 30 minutos sin uso.
- No subas información financiera sensible sin añadir al menos una capa de protección (HTTP Basic, token de acceso o VPN).

## Documentación

- docs/decisiones-iniciales.md - decisiones de arquitectura.
- docs/catalogo-campos.md - catálogo de campos financieros reconocidos.
- docs/despliegue.md - guía de despliegue en Vercel + Render.
