# Decisiones iniciales

## Alcance
Aplicación visual y expositiva para una sola persona.
No se implementa: edición, pagos, auditoría, login, dashboards complejos.

## Arquitectura
- Monolito modular Spring Boot
- React + TypeScript + Vite
- PostgreSQL (Fase 2)

## Capas
1. Importación e interpretación (normalización)
2. Presentación (conserva encabezados originales)

## Monedas
- PEN y USD nunca se suman juntos
- No se inventan tipos de cambio
- Registro USD sin saldo funcional PEN queda pendiente

## Duplicados
Clave: unidad + RUC + tipo doc + número comprobante
Duplicados ambiguos se marcan, no se eliminan