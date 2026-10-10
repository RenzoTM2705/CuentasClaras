# Guía de despliegue — CuentasClaras

Arquitectura de despliegue:

- **Frontend:** Vercel
- **Backend:** Render
- **Base de datos:** ninguna

## Backend en Render

1. Crea una cuenta en https://render.com
2. **New +** → **Web Service**.
3. Conecta tu repositorio de GitHub.
4. Configura:
   - **Name:** `cuentasclaras-backend`
   - **Region:** la más cercana a tus usuarios.
   - **Branch:** `main`
   - **Root Directory:** `backend`
   - **Runtime:** `Java`
   - **Build Command:** `mvn clean package -DskipTests`
   - **Start Command:** `java -jar target/cuentasclaras-backend-0.1.0.jar`
   - **Plan:** Free
5. **Environment Variables:**
   - `SPRING_PROFILES_ACTIVE` = `prod`
   - `CORS_ALLOWED_ORIGINS` = `https://tu-frontend.vercel.app`
   - `MAX_UPLOAD_SIZE_MB` = `50`
6. Crea el servicio.

El backend estará disponible en `https://cuentasclaras-backend.onrender.com`.

> **Nota:** Render Free tarda ~30 s en "despertar" tras un periodo sin uso. Es esperado.

## Frontend en Vercel

1. Crea una cuenta en https://vercel.com
2. **Add New...** → **Project**.
3. Importa tu repositorio de GitHub.
4. Configura:
   - **Framework Preset:** Vite
   - **Root Directory:** `frontend`
   - **Build Command:** `pnpm build`
   - **Output Directory:** `dist`
5. **Environment Variables:**
   - `VITE_API_BASE_URL` = `https://cuentasclaras-backend.onrender.com`
6. Deploy.

## Verificación end-to-end

1. Abre la URL del frontend en Vercel.
2. Sube tu Excel real.
3. Procesa.
4. Verifica que:
   - El mosaico carga.
   - La vista ampliada muestra las filas completas.
   - Los filtros funcionan.
   - La exportación descarga el `.xlsx`.

## Seguridad

El proyecto **no tiene autenticación**. Si el despliegue queda público:

- Cualquiera con la URL puede subir archivos y ver los datos.
- Las sesiones se guardan en memoria y se limpian tras 30 minutos sin uso.
- No subas información financiera sensible sin protección adicional.

Opciones de protección mínima si decides mantenerlo público:

1. **HTTP Basic con Spring Security:** añadir usuario y contraseña simples.
2. **Token de acceso fijo:** pasar un header `Authorization` que el frontend lea de una env var.
3. **Restricción por IP:** en el plan Pro de Render.
4. **VPN o red interna:** si solo lo usas tú.

La opción 1 es la más rápida y suficiente para uso individual.

## Actualizaciones

- `git push origin main` despliega automáticamente en ambos servicios.
- Si cambias variables de entorno, Render y Vercel reinician el servicio automáticamente.