### DBeaver / DataGrip / IntelliJ Database

| Campo         | Valor                      |
|---------------|----------------------------|
| Host          | `localhost`                |
| Puerto        | `5432`                     |
| Base de datos | `codelingo`                |
| Usuario       | `postgres`                 |
| Contraseña    | *(la que configuraste)*    |
| Driver        | PostgreSQL                 |

En IntelliJ: `View → Tool Windows → Database → + → Data Source → PostgreSQL`.

### Consultas útiles una vez conectado

```sql
-- Ver todas las tablas
\dt

-- Ver usuarios registrados
SELECT id, username, email, role FROM users;

-- Ver challenges cargados
SELECT id, title, difficulty FROM challenges;

-- Ver submissions
SELECT s.id, u.username, c.title, s.passed, s.submitted_at
FROM submissions s
JOIN users u ON s.user_id = u.id
JOIN challenges c ON s.challenge_id = c.id
ORDER BY s.submitted_at DESC;
```

---

## 8. Estructura del proyecto

```
src/main/java/com/codelingo/
├── CodelingoApplication.java   # Entry point
├── config/                     # Configuración de Spring Security, CORS, beans
├── controller/                 # Endpoints REST (@RestController)
├── dto/                        # Objetos de transferencia (request / response)
├── model/                      # Entidades JPA (@Entity)
├── repository/                 # Interfaces Spring Data JPA
├── security/                   # Filtros JWT, UserDetailsService
└── service/                    # Lógica de negocio
```

### Endpoints principales

| Método | Ruta                    | Auth requerida |
|--------|-------------------------|----------------|
| POST   | `/api/auth/register`    | No             |
| POST   | `/api/auth/login`       | No             |
| GET    | `/api/challenges`       | JWT            |
| POST   | `/api/submissions`      | JWT            |

---

## 9. Flujo de trabajo con Git

### Convención de nombres de ramas

```
feat/descripcion-corta         # nueva funcionalidad
fix/descripcion-del-bug        # corrección de bug
refactor/que-se-refactoriza    # refactor sin cambio de comportamiento
docs/lo-que-se-documenta       # solo documentación
test/lo-que-se-testea          # solo tests
```

### Paso a paso para trabajar en una feature

```bash
# 1. Asegurarte de estar en main y actualizado
git checkout main
git pull origin main

# 2. Crear la rama nueva
git checkout -b feat/mi-nueva-feature

# 3. Hacer cambios, luego stagear y commitear
git add src/main/java/com/codelingo/controller/MiController.java
git commit -m "feat: agregar endpoint para listar X"

# 4. Si mientras trabajás, main recibió cambios, incorporarlos
git fetch origin main
git rebase origin/main

# 5. Pushear y abrir PR
git push -u origin feat/mi-nueva-feature
```

Después en GitHub → **New Pull Request** → base: `main` ← compare: `feat/mi-nueva-feature`.

### Convención de mensajes de commit

```
feat:      nueva funcionalidad
fix:       corrección de bug
refactor:  cambio de código sin cambio de comportamiento
docs:      documentación
test:      agregar o modificar tests
chore:     cambios de build, dependencias, configuración
```

### Checklist antes de abrir un PR

- [ ] `mvn test` pasa sin errores
- [ ] No hay credenciales hardcodeadas en el código
- [ ] El PR tiene un título y descripción que explican qué cambia y por qué
- [ ] Se probó manualmente el endpoint nuevo/modificado

---

## 10. Problemas frecuentes

**`Connection refused` al conectar a PostgreSQL**
```bash
# Verificar que el servicio corra
pg_isready -h localhost -p 5432

# Linux: levantar el servicio si está caído
sudo systemctl start postgresql
```

**`could not translate host name "db"`**
Ese hostname existe solo dentro de la red Docker. Para desarrollo local usá `localhost` en `DB_URL`.

**Puerto 8080 ya en uso**
```bash
# Ver qué proceso lo usa
lsof -i :8080        # macOS / Linux
netstat -ano | findstr :8080  # Windows

# Levantar en otro puerto
PORT=8081 mvn spring-boot:run
```

**Error 403 en todos los endpoints (después de login)**
Verificá que estás enviando el header `Authorization: Bearer <token>` en los requests autenticados.

**Las tablas no se crean**
Verificar que la BD `codelingo` existe y que las credenciales son correctas. Revisar los logs
al iniciar la app, Hibernate loggea los CREATE TABLE que ejecuta.