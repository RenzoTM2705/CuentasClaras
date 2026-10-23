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
| Fase 1 — Fundación | OK |
| Fase 2 — Modelo en memoria y contrato API | OK |
| Fase 3 — Importación inteligente de Excel | OK |
| Fase 4 — Combinación y deduplicación | OK |
| Fase 5 — Selector visual de vistas | OK |
| Fase 6 — Análisis financiero | OK |
| Fase 7 — Exportación | OK |
| Fase 8 — Pruebas y despliegue | OK |

Ver `CHANGELOG.md` para el detalle por fase.

## Estructura del repositorio
cuentasclaras/
├── frontend/ # React + TypeScript + Vite + Tailwind (pnpm)
├── backend/ # Java 17 + Spring Boot
├── docs/ # Documentación técnica y de decisiones
└── .github/ # CI


## Requisitos locales

- Node.js 20+
- pnpm 9+
- Java 17+
- Maven 3.9+

No se requiere PostgreSQL: el análisis es **efímero en memoria**.

## Configuración

1. Copia `.env.example` a `.env` en la raíz y ajusta si es necesario.
2. Copia `frontend/.env.example` a `frontend/.env`.

Variables relevantes:
VITE_API_BASE_URL=http://localhost:8080
SERVER_PORT=8080
CORS_ALLOWED_ORIGINS=http://localhost:5173
MAX_UPLOAD_SIZE_MB=50


## Ejecución local

### Backend
cd backend
mvn spring-boot:run


Disponible en `http://localhost:8080`.

### Frontend
cd frontend
pnpm install
pnpm dev


Disponible en `http://localhost:5173`.

## Flujo de uso

1. Abrir `http://localhost:5173`.
2. Arrastrar uno o varios archivos Excel.
3. Elegir el modo de procesamiento:
   - **Combinar**: una sola vista unificada.
   - **Independiente**: una vista por archivo.
   - **Ambos**: una vista por archivo + una vista combinada.
4. Pulsar **Procesar archivos**.
5. En el mosaico, pasar el cursor sobre una región y pulsar el icono de ampliación.
6. Dentro de la vista ampliada:
   - Ver el resumen general (PEN y USD separados).
   - Ver el resumen por proveedor.
   - Ver la tabla completa con encabezados originales.
7. Volver al mosaico con **Volver al mosaico** o `Esc`.
8. **Subir más archivos** para empezar de nuevo.

## Reglas financieras

- **PEN y USD nunca se suman juntos.** Se muestran siempre por separado.
- No se inventan tipos de cambio.
- Un registro PEN con columnas USD vacías se procesa normalmente.
- Un registro USD sin saldo funcional PEN válido queda con equivalencia pendiente.
- La columna `DIAS A VENCER` se conserva como texto descriptivo; el vencimiento real se calculará en la Fase 6.

## Flujo de ramas
main ← versión estable
develop ← integración
feature/* ← una rama por fase


Reglas:

- Nunca se desarrolla directamente sobre `main`.
- Cada fase se trabaja en su propia rama `feature/*` creada desde `develop`.
- Al terminar: `feature/*` → `develop` (merge `--no-ff`).
- Cuando `develop` está estable: `develop` → `main`.

## Documentación

- `docs/decisiones-iniciales.md` — decisiones de arquitectura y alcance.
- `docs/catalogo-campos.md` — catálogo de campos financieros reconocidos.
