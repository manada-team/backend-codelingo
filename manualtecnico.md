# Manual Técnico — CodeLingo

**Versión:** 1.0  
**Fecha:** Junio 2026  
**Equipo:** Manada Team

---

## Índice

1. [Descripción del sistema](#1-descripción-del-sistema)
2. [Arquitectura general](#2-arquitectura-general)
3. [Tecnologías utilizadas](#3-tecnologías-utilizadas)
4. [Backend — Spring Boot](#4-backend--spring-boot)
    - 4.1 [Estructura del proyecto](#41-estructura-del-proyecto)
    - 4.2 [Modelos de datos](#42-modelos-de-datos)
    - 4.3 [Endpoints REST](#43-endpoints-rest)
    - 4.4 [Seguridad y autenticación JWT](#44-seguridad-y-autenticación-jwt)
    - 4.5 [Motor de ejecución de código](#45-motor-de-ejecución-de-código)
    - 4.6 [Sistema de XP y progreso](#46-sistema-de-xp-y-progreso)
    - 4.7 [Servicio de email](#47-servicio-de-email)
5. [Frontend — React](#5-frontend--react)
    - 5.1 [Estructura del proyecto](#51-estructura-del-proyecto)
    - 5.2 [Componentes principales](#52-componentes-principales)
    - 5.3 [Flujo de autenticación](#53-flujo-de-autenticación)
    - 5.4 [Sistema de temas](#54-sistema-de-temas)
6. [Base de datos — PostgreSQL](#6-base-de-datos--postgresql)
    - 6.1 [Entidades y relaciones](#61-entidades-y-relaciones)
    - 6.2 [Diagrama entidad-relación](#62-diagrama-entidad-relación)
7. [Despliegue con Docker](#7-despliegue-con-docker)
8. [Variables de entorno](#8-variables-de-entorno)
9. [Flujo de datos del sistema](#9-flujo-de-datos-del-sistema)
10. [Decisiones de diseño](#10-decisiones-de-diseño)

---

## 1. Descripción del sistema

**CodeLingo** es una plataforma web educativa gamificada para aprender programación. Funciona como un juego de niveles donde el usuario resuelve desafíos de código en Python, Java o C, acumula XP, mantiene rachas diarias y compite en un ranking global.

El sistema está dividido en dos repositorios:

| Repositorio | Descripción |
|---|---|
| `manada-team/backend-codelingo` | API REST en Java / Spring Boot |
| `manada-team/codelingo` | SPA en React |

---

## 2. Arquitectura general

```
┌─────────────────────────────────┐
│        Browser del usuario      │
│   React SPA (puerto 3000)       │
└──────────────┬──────────────────┘
               │ HTTP + JWT
               │ REACT_APP_API_URL
               ▼
┌─────────────────────────────────┐
│   Backend Spring Boot           │
│   (puerto 8080 / 8081)          │
│                                 │
│  AuthController                 │
│  UserController                 │
│  LevelController                │
│  ExecutionController            │
│  LevelGroupController           │
│  FunFactController              │
│  HealthController               │
└──────────────┬──────────────────┘
               │ JPA / Hibernate
               ▼
┌─────────────────────────────────┐
│   PostgreSQL (puerto 5432)      │
│   Base de datos: codelingo      │
└─────────────────────────────────┘
```

La comunicación entre frontend y backend ocurre exclusivamente vía HTTP/JSON. Todos los endpoints (excepto `/api/auth/**` y `/api/health`) requieren autenticación mediante JWT en el header `Authorization: Bearer <token>`.

---

## 3. Tecnologías utilizadas

### Backend

| Tecnología | Versión | Rol |
|---|---|---|
| Java | 17 | Lenguaje |
| Spring Boot | 3.2.4 | Framework principal |
| Spring Security | 6.x | Autenticación y autorización |
| Spring Data JPA | 3.x | Acceso a datos / ORM |
| Hibernate | 6.x | Implementación JPA |
| PostgreSQL | 15+ | Base de datos relacional |
| JJWT | 0.12.5 | Generación y validación de tokens JWT |
| Lombok | 1.18+ | Reducción de boilerplate |
| SendGrid | 4.10.2 | Envío de emails transaccionales |
| Maven | 3.9 | Gestión de dependencias y build |
| Docker | 24+ | Containerización |

### Frontend

| Tecnología | Versión | Rol |
|---|---|---|
| React | 19 | Framework UI |
| Create React App | 5.x | Tooling / bundler |
| Monaco Editor | última | Editor de código en browser |
| JavaScript (ES2022) | — | Lenguaje |
| CSS3 | — | Estilos (sin framework) |

---

## 4. Backend — Spring Boot

### 4.1 Estructura del proyecto

```
backend-codelingo/
├── Dockerfile
├── pom.xml
├── dump.sql                          # Datos iniciales de la BD
└── src/
    ├── main/
    │   ├── java/com/codelingo/
    │   │   ├── CodelingoApplication.java   # Entry point (@SpringBootApplication)
    │   │   ├── config/                     # Configuración de beans
    │   │   │   ├── SecurityConfig.java     # Cadena de filtros HTTP
    │   │   │   └── CorsConfig.java         # Política CORS
    │   │   ├── controller/                 # Capa HTTP
    │   │   │   ├── AuthController.java
    │   │   │   ├── UserController.java
    │   │   │   ├── LevelController.java
    │   │   │   ├── LevelGroupController.java
    │   │   │   ├── ExecutionController.java
    │   │   │   ├── FunFactController.java
    │   │   │   ├── HealthController.java
    │   │   │   └── GlobalExceptionHandler.java
    │   │   ├── dto/                        # Objetos de transferencia
    │   │   ├── model/                      # Entidades JPA
    │   │   │   ├── User.java
    │   │   │   ├── Role.java (enum)
    │   │   │   ├── Level.java
    │   │   │   ├── LevelGroup.java
    │   │   │   ├── UserProgress.java
    │   │   │   ├── FunFact.java
    │   │   │   ├── EmailVerificationToken.java
    │   │   │   └── PasswordResetToken.java
    │   │   ├── repository/                 # Interfaces Spring Data JPA
    │   │   ├── security/                   # Filtros JWT y UserDetailsService
    │   │   └── service/                    # Lógica de negocio
    │   │       ├── AuthService.java
    │   │       ├── LevelService.java
    │   │       ├── CodeExecutionService.java
    │   │       ├── EmailService.java
    │   │       └── StreakService.java
    │   └── resources/
    │       └── application.properties
    └── test/
```

### 4.2 Modelos de datos

#### `User`

Entidad central del sistema. Almacena credenciales, progreso y preferencias del jugador.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `Long` | PK autoincremental |
| `username` | `String` | Nombre de usuario (único) |
| `email` | `String` | Correo electrónico (único) |
| `password` | `String` | Hash BCrypt |
| `role` | `Role` (enum) | `PLAYER` o `ADMIN` |
| `totalXp` | `int` | XP total acumulado |
| `xpPython` | `int` | XP acumulado en Python |
| `xpJava` | `int` | XP acumulado en Java |
| `xpC` | `int` | XP acumulado en C |
| `currentStreak` | `int` | Racha actual en días |
| `longestStreak` | `int` | Racha más larga alcanzada |
| `lastActivityDate` | `LocalDate` | Última fecha de actividad |
| `activeLanguage` | `String` | Lenguaje actualmente seleccionado |
| `startedLanguages` | `String` | Lenguajes iniciados (ej: `"python,java"`) |
| `theme` | `String` | Tema visual seleccionado |
| `emailVerified` | `boolean` | Si el email fue verificado |
| `createdAt` | `LocalDateTime` | Fecha de registro |

#### `Level`

Representa un desafío de programación dentro de un grupo.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `Long` | PK autoincremental |
| `levelNumber` | `int` | Número de nivel (único) |
| `title` | `String` | Título del desafío |
| `description` | `TEXT` | Descripción explicativa |
| `challengeContent` | `TEXT` | Enunciado del desafío |
| `expectedOutput` | `TEXT` | Salida esperada para validar |
| `xpReward` | `int` | XP que otorga completarlo (default: 10) |
| `levelGroup` | `LevelGroup` | Grupo al que pertenece |

#### `LevelGroup`

Agrupa niveles por categoría o lenguaje.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `Long` | PK autoincremental |
| `name` | `String` | Nombre del grupo |
| `language` | `String` | Lenguaje asociado (`python`, `java`, `c`) |
| `levels` | `List<Level>` | Niveles del grupo |

#### `UserProgress`

Registra si un usuario completó un nivel específico.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `Long` | PK autoincremental |
| `user` | `User` | FK al usuario |
| `level` | `Level` | FK al nivel |
| `completed` | `boolean` | Si lo completó |
| `completedAt` | `LocalDateTime` | Cuándo lo completó |

#### `FunFact`

Dato curioso mostrado en la pantalla de inicio.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `Long` | PK autoincremental |
| `content` | `TEXT` | Contenido del dato curioso |

#### `EmailVerificationToken` / `PasswordResetToken`

Tokens temporales para verificación de email y recuperación de contraseña. Contienen el token (UUID), referencia al usuario y fecha de expiración.

### 4.3 Endpoints REST

#### Autenticación (`/api/auth`)

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| `POST` | `/api/auth/register` | No | Registrar nuevo usuario |
| `POST` | `/api/auth/login` | No | Login; devuelve JWT |
| `POST` | `/api/auth/verify-email` | No | Verificar email con token |
| `POST` | `/api/auth/resend-verification` | No | Reenviar email de verificación |
| `POST` | `/api/auth/forgot-password` | No | Solicitar reset de contraseña |
| `POST` | `/api/auth/reset-password` | No | Resetear contraseña con token |

**Respuesta de login:**
```json
{
  "token": "<JWT>",
  "username": "johndoe",
  "role": "PLAYER"
}
```

#### Usuarios (`/api/users`)

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| `GET` | `/api/users/me` | JWT | Perfil del usuario autenticado |
| `PUT` | `/api/users/me` | JWT | Actualizar perfil |
| `POST` | `/api/users/me/theme` | JWT | Cambiar tema visual |
| `GET` | `/api/users/me/progress` | JWT | Progreso del usuario (niveles completados) |
| `GET` | `/api/users/leaderboard` | JWT | Ranking global por XP |
| `GET` | `/api/users` | ADMIN | Listar todos los usuarios |
| `DELETE` | `/api/users/{id}` | ADMIN | Eliminar usuario |
| `PUT` | `/api/users/{id}/role` | ADMIN | Cambiar rol de usuario |

#### Niveles (`/api/levels`)

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| `GET` | `/api/levels` | JWT | Listar todos los niveles |
| `GET` | `/api/levels/{id}` | JWT | Obtener nivel por ID |
| `POST` | `/api/levels/{id}/check` | JWT | Verificar respuesta del usuario |
| `POST` | `/api/levels` | ADMIN | Crear nivel |
| `PUT` | `/api/levels/{id}` | ADMIN | Editar nivel |
| `DELETE` | `/api/levels/{id}` | ADMIN | Eliminar nivel |

**Body para verificar respuesta:**
```json
{ "answer": "42" }
```

**Respuesta de verificación:**
```json
{
  "correct": true,
  "message": "¡Correcto! +10 XP",
  "xpEarned": 10
}
```

#### Grupos de niveles (`/api/level-groups`)

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| `GET` | `/api/level-groups` | JWT | Listar grupos |
| `POST` | `/api/level-groups` | ADMIN | Crear grupo |
| `DELETE` | `/api/level-groups/{id}` | ADMIN | Eliminar grupo |

#### Ejecución de código (`/api/execute`)

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| `POST` | `/api/execute` | JWT | Ejecutar código y devolver salida |

**Body:**
```json
{
  "code": "print('Hola')",
  "language": "python"
}
```

**Respuesta:**
```json
{
  "output": "Hola\n",
  "stderr": "",
  "exitCode": 0,
  "timeMs": 142,
  "error": null
}
```

#### Otros

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| `GET` | `/api/health` | No | Health check del servidor |
| `GET` | `/api/fun-facts/random` | JWT | Dato curioso aleatorio |

### 4.4 Seguridad y autenticación JWT

El sistema usa **Spring Security** con autenticación stateless basada en JWT.

**Flujo de autenticación:**

```
1. POST /api/auth/login  { username, password }
        │
        ▼
2. AuthService valida credenciales con BCrypt
        │
        ▼
3. Se genera JWT firmado con clave secreta (HS256 / HS512)
   Payload: { sub: username, iat, exp }
        │
        ▼
4. Cliente almacena el token en localStorage
        │
        ▼
5. Cada request posterior envía:
   Authorization: Bearer <token>
        │
        ▼
6. JwtAuthenticationFilter (OncePerRequestFilter):
   - Extrae y valida el token
   - Carga el UserDetails
   - Setea el SecurityContext
```

**Roles:**
- `PLAYER` — usuario estándar, puede jugar y ver su progreso
- `ADMIN` — acceso total: gestión de usuarios, niveles y grupos

**Rutas públicas** (sin JWT):
- `POST /api/auth/**`
- `GET /api/health`

### 4.5 Motor de ejecución de código

`CodeExecutionService` ejecuta código del usuario de forma segura en el servidor.

**Lenguajes soportados:**

| Lenguaje | Archivo | Comando |
|---|---|---|
| Python | `solution.py` | `python3 solution.py` |
| Java | `Main.java` | `javac Main.java && java Main` |
| C | `solution.c` | `gcc solution.c -o solution && ./solution` |

**Proceso de ejecución:**

```
1. Se crea un directorio temporal único (Files.createTempDirectory)
2. Se escribe el código en el archivo correspondiente
3. Se lanza un ProcessBuilder con el comando del lenguaje
4. Se leen stdout y stderr de forma asíncrona (threads daemon)
5. Se espera hasta TIMEOUT_SECONDS (10 segundos)
   - Si expira → proceso destruido → respuesta de timeout
6. Se trunca la salida si supera MAX_OUTPUT_CHARS (50.000 chars)
7. Se elimina el directorio temporal (siempre, en bloque finally)
```

**Consideraciones de seguridad:**
- Cada ejecución corre en un directorio temporal aislado que se elimina tras terminar.
- El timeout de 10 segundos previene loops infinitos.
- El Dockerfile instala Python 3, GCC y OpenJDK 17 en la imagen runtime para soportar los tres lenguajes.

### 4.6 Sistema de XP y progreso

Cuando un usuario envía la respuesta correcta a un nivel (`POST /api/levels/{id}/check`):

1. `LevelService` compara la respuesta con `expectedOutput` (normalización: trim + lowercase).
2. Si es correcta y el nivel no fue completado antes:
    - Se crea o actualiza el registro `UserProgress` con `completed = true`.
    - Se suma `xpReward` al `totalXp` del usuario y al campo específico del lenguaje (`xpPython`, `xpJava`, `xpC`).
    - Se invoca `StreakService` para actualizar la racha del usuario.
3. La respuesta incluye `xpEarned` para mostrarlo en el frontend.

**Lógica de rachas (`StreakService`):**
- Si `lastActivityDate` fue ayer → `currentStreak++`.
- Si fue hoy → no cambia (ya contabilizado).
- Si fue antes de ayer → `currentStreak = 1` (racha rota).
- Si `currentStreak > longestStreak` → actualiza `longestStreak`.

### 4.7 Servicio de email

`EmailService` usa **SendGrid** para envíos transaccionales:

- **Verificación de email:** al registrarse, se envía un email con un token UUID. El usuario debe hacer clic para activar su cuenta.
- **Recuperación de contraseña:** se envía un token de reset con expiración. El usuario ingresa el token y la nueva contraseña.

Los tokens se almacenan en `EmailVerificationToken` y `PasswordResetToken` con fecha de expiración. Tokens expirados son rechazados.

---

## 5. Frontend — React

### 5.1 Estructura del proyecto

```
codelingo/
├── package.json
├── .env.local                    # No commiteado (ver Variables de entorno)
└── src/
    ├── index.js                  # Entry point: ReactDOM.render
    ├── index.css                 # Reset y variables CSS globales
    ├── App.js                    # Componente raíz: rutas, estado global, navbar
    ├── App.css                   # Estilos de layout y navbar
    └── components/
        ├── AuthScreen.js/.css    # Login y registro
        ├── HomeScreen.js/.css    # Pantalla de inicio: estadísticas, ranking, fun fact
        ├── GameScreen.js/.css    # Pantalla de juego: niveles + editor Monaco
        ├── ProfileScreen.js/.css # Perfil del usuario y estadísticas detalladas
        ├── AdminScreen.js/.css   # Panel de administración (solo ADMIN)
        └── ThemeSelector.js/.css # Selector de tema visual
```

### 5.2 Componentes principales

#### `App.js` — Controlador de navegación

Es el componente raíz. Maneja:
- **Estado de sesión:** lee `token`, `username` y `role` de `localStorage` al iniciar.
- **Navegación:** variable `screen` con estados `'home'`, `'game'`, `'profile'`, `'admin'`.
- **Tema:** aplica el atributo `data-theme` en `document.documentElement`.
- **Navbar:** menú de navegación con acceso condicional al panel Admin.

```jsx
// Flujo principal:
if (!user) → <AuthScreen />
else       →¿ navbar + contenido según screen
```

#### `AuthScreen.js` — Login y registro

- Formulario de login/registro con toggle entre modos.
- Llama a `POST /api/auth/login` o `POST /api/auth/register`.
- Al éxito de login, guarda `token`, `username` y `role` en `localStorage` y llama a `onAuthSuccess`.
- Maneja errores de validación y muestra mensajes al usuario.

#### `HomeScreen.js` — Pantalla de inicio

- Muestra estadísticas del usuario: XP por lenguaje, racha actual, niveles completados.
- Botones para iniciar juego en cada lenguaje.
- Muestra un `FunFact` aleatorio obtenido de `/api/fun-facts/random`.
- Muestra ranking global (top 10) desde `/api/users/leaderboard`.

#### `GameScreen.js` — Pantalla principal del juego

Es el componente más complejo. Está dividido en dos paneles:

**Panel izquierdo — Desafíos:**
- Carga los niveles del lenguaje activo desde `/api/levels`.
- Carga el progreso del usuario desde `/api/users/me/progress`.
- Navega entre niveles con un dropdown que muestra el estado de cada uno (completado ✅ / en curso ▶ / pendiente ○).
- El usuario escribe su respuesta en un input y la envía a `/api/levels/{id}/check`.
- Muestra feedback inmediato: correcto (+XP) o incorrecto.
- Avanza automáticamente al siguiente nivel tras responder correctamente.

**Panel derecho — Editor de código:**
- Integra **Monaco Editor** (el mismo editor de VS Code).
- Soporta Python, Java y C con syntax highlighting.
- Botón "Ejecutar" envía el código a `/api/execute` y muestra stdout/stderr en la consola inferior.
- Muestra el tiempo de ejecución en milisegundos.

#### `ProfileScreen.js` — Perfil del usuario

- Muestra información del usuario: username, email, fecha de registro.
- Estadísticas: XP total, XP por lenguaje, racha actual, racha más larga, niveles completados.
- Permite cambiar username y contraseña.
- Permite eliminar la cuenta.

#### `AdminScreen.js` — Panel de administración

Visible solo para usuarios con rol `ADMIN`. Permite:
- Listar, crear, editar y eliminar niveles.
- Listar, crear y eliminar grupos de niveles.
- Listar usuarios, cambiar su rol y eliminarlos.

#### `ThemeSelector.js` — Selector de tema

Dropdown que permite cambiar el tema visual. Al seleccionar:
1. Actualiza `localStorage`.
2. Llama a `POST /api/users/me/theme` para persistir en el servidor.
3. Aplica el atributo `data-theme` en el `<html>` para que las variables CSS cambien globalmente.

### 5.3 Flujo de autenticación

```
1. Usuario abre la app
   └─ App.js lee localStorage
      ├─ Si hay token → carga la sesión y muestra la app
      └─ Si no hay token → muestra AuthScreen

2. Login exitoso
   └─ Backend devuelve { token, username, role }
      └─ Se guardan en localStorage
         └─ App.js actualiza el estado → muestra la app

3. Logout
   └─ Se limpia localStorage (token, username, role, activeLanguage, theme)
      └─ App.js resetea el estado → muestra AuthScreen

4. Requests autenticados
   └─ Todos los fetch incluyen:
      Authorization: Bearer <token>  (leído de localStorage)
```

### 5.4 Sistema de temas

Los temas se implementan con **custom properties de CSS**. El atributo `data-theme` en `<html>` controla qué conjunto de variables se aplica.

```css
/* Ejemplo simplificado */
:root { --bg: #1a1a2e; --accent: #00ff88; }
[data-theme="retro"] { --bg: #0d0d0d; --accent: #ff6b35; }
[data-theme="ocean"] { --bg: #0a1628; --accent: #00d4ff; }
```

El tema se persiste en `localStorage` y en el campo `theme` del usuario en base de datos, para que se restaure al iniciar sesión desde cualquier dispositivo.

---

## 6. Base de datos — PostgreSQL

### 6.1 Entidades y relaciones

```
users (1) ──────────── (*) user_progress
  │                          │
  │                          │
  │                    (*) levels (*)
  │                          │
  │                          │
  │                    (1) level_groups
  │
  └── email_verification_tokens (1:1)
  └── password_reset_tokens (1:1)

fun_facts (tabla independiente)
```

**Claves foráneas:**
- `user_progress.user_id → users.id`
- `user_progress.level_id → levels.id`
- `levels.level_group_id → level_groups.id`
- `email_verification_tokens.user_id → users.id`
- `password_reset_tokens.user_id → users.id`

### 6.2 Diagrama entidad-relación

```
┌──────────────────┐       ┌──────────────────────┐
│      users       │       │    user_progress      │
├──────────────────┤       ├──────────────────────┤
│ id (PK)          │──────<│ id (PK)              │
│ username         │       │ user_id (FK)         │
│ email            │       │ level_id (FK)        │
│ password         │       │ completed            │
│ role             │       │ completed_at         │
│ total_xp         │       └──────────┬───────────┘
│ xp_python        │                  │
│ xp_java          │                  │
│ xp_c             │       ┌──────────▼───────────┐
│ current_streak   │       │       levels          │
│ longest_streak   │       ├──────────────────────┤
│ last_activity_date│      │ id (PK)              │
│ active_language  │       │ level_number         │
│ started_languages│       │ title                │
│ theme            │       │ description          │
│ email_verified   │       │ challenge_content    │
│ created_at       │       │ expected_output      │
└──────────────────┘       │ xp_reward            │
                           │ level_group_id (FK)  │
                           └──────────┬───────────┘
                                      │
                           ┌──────────▼───────────┐
                           │    level_groups       │
                           ├──────────────────────┤
                           │ id (PK)              │
                           │ name                 │
                           │ language             │
                           └──────────────────────┘
```

**Datos iniciales:** el archivo `dump.sql` contiene el schema completo y los datos de niveles precargados. Se carga con:

```bash
psql -U postgres -d codelingo -f dump.sql
```

---

## 7. Despliegue con Docker

El backend tiene un `Dockerfile` multistage:

```dockerfile
# Stage 1: Build con Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B -q   # Caché de dependencias
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true -q

# Stage 2: Runtime mínimo
FROM eclipse-temurin:17-jre-alpine
# Instala los runtimes necesarios para ejecutar código de usuarios
RUN apk add --no-cache python3 gcc musl-dev openjdk17
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Build y ejecución:**

```bash
# Construir imagen
docker build -t codelingo-backend .

# Ejecutar con variables de entorno
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/codelingo \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=tu_password \
  -e JWT_SECRET=tu_clave_secreta \
  -e SENDGRID_API_KEY=tu_api_key \
  codelingo-backend
```

> El Stage 1 instala Python 3, GCC y OpenJDK 17 en la imagen runtime para que `CodeExecutionService` pueda ejecutar los tres lenguajes soportados.

---

## 8. Variables de entorno

### Backend (`application.properties` / variables de entorno)

| Variable | Descripción | Ejemplo |
|---|---|---|
| `DB_URL` | URL JDBC de PostgreSQL | `jdbc:postgresql://localhost:5432/codelingo` |
| `DB_USERNAME` | Usuario de PostgreSQL | `postgres` |
| `DB_PASSWORD` | Contraseña de PostgreSQL | `mi_password` |
| `JWT_SECRET` | Clave secreta para firmar JWT (mín. 256 bits) | `cadena_muy_larga_y_aleatoria` |
| `SENDGRID_API_KEY` | API key de SendGrid para emails | `SG.xxx...` |
| `SENDGRID_FROM_EMAIL` | Email remitente | `noreply@codelingo.com` |
| `APP_URL` | URL base del frontend (para links en emails) | `http://localhost:3000` |

### Frontend (`.env.local`)

| Variable | Descripción | Valor por defecto en código |
|---|---|---|
| `REACT_APP_API_URL` | URL base del backend | `http://localhost:8081` |

> Las variables de React **deben** empezar con `REACT_APP_` para que Create React App las incluya en el bundle. Tras modificar `.env.local` hay que reiniciar el servidor de desarrollo.

---

## 9. Flujo de datos del sistema

### Flujo completo: usuario completa un nivel

```
[Frontend]                          [Backend]                    [BD]
    │                                    │                         │
    │  POST /api/levels/{id}/check       │                         │
    │  { "answer": "42" }                │                         │
    │  Authorization: Bearer <jwt>       │                         │
    ├───────────────────────────────────>│                         │
    │                                    │  JwtAuthenticationFilter│
    │                                    │  valida JWT             │
    │                                    │                         │
    │                                    │  LevelController        │
    │                                    │  → LevelService         │
    │                                    │    .checkAnswer(id, ans)│
    │                                    │                         │
    │                                    │  Busca Level por id     │
    │                                    │─────────────────────────>
    │                                    │                         │ SELECT
    │                                    │<─────────────────────────
    │                                    │                         │
    │                                    │  Normaliza y compara    │
    │                                    │  answer vs expectedOutput│
    │                                    │                         │
    │                                    │  Si correcto:           │
    │                                    │  - Crea/actualiza       │
    │                                    │    UserProgress         │
    │                                    │─────────────────────────>
    │                                    │                         │ INSERT/UPDATE
    │                                    │  - Suma XP al User      │
    │                                    │─────────────────────────>
    │                                    │                         │ UPDATE users
    │                                    │  - StreakService        │
    │                                    │    actualiza racha      │
    │                                    │─────────────────────────>
    │                                    │                         │ UPDATE users
    │                                    │                         │
    │  { correct: true,                  │                         │
    │    message: "¡Correcto! +10 XP",   │                         │
    │    xpEarned: 10 }                  │                         │
    │<───────────────────────────────────│                         │
    │                                    │                         │
    │  Muestra badge +10 XP              │                         │
    │  Marca nivel como completado ✅    │                         │
    │  Habilita botón "Siguiente →"      │                         │
```

### Flujo de ejecución de código

```
[Frontend]                     [Backend]                [Sistema de archivos]
    │                               │                          │
    │  POST /api/execute            │                          │
    │  { code, language }           │                          │
    ├──────────────────────────────>│                          │
    │                               │  CodeExecutionService    │
    │                               │  .execute(code, lang)    │
    │                               │                          │
    │                               │  createTempDirectory()   │
    │                               │─────────────────────────>│
    │                               │  writeString(code)       │
    │                               │─────────────────────────>│
    │                               │                          │
    │                               │  ProcessBuilder.start()  │
    │                               │  (python3/gcc+run/javac) │
    │                               │                          │
    │                               │  waitFor(10s timeout)    │
    │                               │                          │
    │                               │  deleteTempDir()         │
    │                               │─────────────────────────>│
    │                               │                          │
    │  { output, stderr,            │                          │
    │    exitCode, timeMs }         │                          │
    │<──────────────────────────────│                          │
    │                               │                          │
    │  Muestra en consola           │                          │
```

---

## 10. Decisiones de diseño

### Autenticación stateless con JWT

Se eligió JWT en lugar de sesiones del servidor para facilitar el escalado horizontal. El token contiene el `username` y el backend puede validarlo sin consultar la base de datos en cada request.

### Ejecución de código en proceso hijo

El código del usuario se ejecuta directamente como proceso del sistema operativo dentro del mismo servidor. Esto es simple y funcional para un entorno educativo, pero **no es sandboxing real**. Para producción en escala se recomienda migrar a contenedores Docker por ejecución o a un servicio externo de ejecución de código.

### Sin framework de routing en el frontend

En lugar de React Router, la navegación se maneja con una variable de estado `screen` en `App.js`. Esto simplifica la implementación para la escala actual del proyecto (4 pantallas).

### XP por lenguaje

Además del `totalXp` global, se mantienen contadores separados (`xpPython`, `xpJava`, `xpC`) para mostrar progreso granular por lenguaje en el perfil y la pantalla de inicio.

### Temas con CSS custom properties

El sistema de temas no requiere librerías externas. Un atributo `data-theme` en el elemento raíz y reglas CSS con `:root` y selectores de atributo es suficiente para theming global eficiente.

### Persistencia del tema en el servidor

El tema se guarda tanto en `localStorage` (para acceso inmediato sin request) como en la base de datos (para sincronización entre dispositivos). La actualización al servidor se hace de forma asíncrona con `try/catch` silencioso para no bloquear la UX.
