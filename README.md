# SIW Football — Gestión de Torneos de Fútbol

Proyecto obligatorio para la asignatura **Sistemi Informativi su Web (SIW)** — Università degli Studi Roma Tre.

Aplicación web completa para gestionar torneos de fútbol: equipos, jugadores, árbitros, partidos y clasificaciones en tiempo real.

---

## Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Backend | Java 21, Spring Boot 3.4.5 |
| Persistencia | Spring Data JPA, Hibernate, PostgreSQL 15 |
| Frontend | Thymeleaf 3.1.3, HTML5, CSS3 |
| Componentes dinámicos | React 18 via CDN (sin build step) |
| Seguridad | Spring Security 6, BCrypt |
| Base de datos | PostgreSQL en Docker (puerto 5435) |

---

## Arquitectura — 3 Capas

```
Presentation  ->  Controller  (HTTP, Thymeleaf, REST)
Business      ->  Service     (@Transactional, lógica de negocio)
Persistence   ->  Repository  (Spring Data JPA, JPQL)
```

Los controllers no acceden al repositorio directamente. Toda la lógica de negocio reside en el service layer.

---

## Modelo de datos

| Entidad | Campos clave |
|---------|-------------|
| `User` | username, password (BCrypt), role (ADMIN/USER) |
| `Tournament` | name, description, startDate |
| `Team` | name, foundationYear, city |
| `Player` | name, surname, position, birthDate, height, team |
| `Referee` | name, surname, refereeCode |
| `Match` | homeTeam, awayTeam, tournament, referee, homeScore, awayScore, status (SCHEDULED/PLAYED), location, matchDate |
| `Comment` | content, match, user |

### Relaciones JPA

- `Tournament` 1:N `Match` — cascade ALL
- `Match` 1:N `Comment` — cascade ALL
- `Team` N:M `Tournament` — tabla intermedia `team_tournament`
- `Player` N:1 `Team` — FetchType.LAZY
- `Match` N:1 `Team` (home/away) — @JoinColumn explícito para evitar conflicto de nombres

Todas las relaciones usan `FetchType.LAZY` por defecto para evitar el problema N+1.

---

## Cómo ejecutar

### 1. Base de datos

```bash
docker-compose up -d
```

Levanta PostgreSQL en `localhost:5435` con base de datos `siw_football`.

### 2. Aplicación

```bash
./mvnw spring-boot:run
```

Acceder en: `http://localhost:8081`

### Credenciales por defecto

| Usuario | Contraseña | Rol |
|---------|-----------|-----|
| `admin` | `admin` | ADMIN |
| `pablo` | `1234` | USER |

### Resetear datos de demo

```sql
TRUNCATE match, player, team_tournament, team, tournament, referee, users RESTART IDENTITY CASCADE;
```

Reiniciar la aplicación. `DataInitializer` inserta: 3 torneos, 12 equipos, ~44 jugadores, 6 árbitros, 35 partidos.

---

## Funcionalidades

### Acceso público (sin autenticación)

- Lista de torneos activos
- Detalle de torneo: equipos participantes, clasificación en tiempo real, calendario paginado
- Acta del partido: resultado, árbitro, estadio, comentarios
- Lista de equipos con plantillas
- Lista de jugadores con búsqueda y filtros
- Lista de árbitros

### Usuario autenticado (rol USER)

- Publicar comentarios en partidos
- Editar sus propios comentarios (con verificación de propiedad en el service layer)

### Administrador (rol ADMIN)

- CRUD completo de torneos, equipos, jugadores, árbitros y partidos
- Registrar resultados de partidos
- Gestionar equipos participantes por torneo
- Eliminar comentarios de cualquier usuario

---

## Clasificación — React 18 + REST API

La tabla de clasificación se renderiza con **React 18 via CDN** (sin npm, webpack ni paso de compilación).

- **Endpoint REST:** `GET /api/tournament/{id}/standings` → JSON
- **Componente:** `/static/js/standings.js` — usa `React.createElement()` puro, sin JSX
- **Integración Thymeleaf:** inyecta el ID del torneo como variable JS global para evitar conflicto con attoparser
- **Ordenación:** puntos DESC, diferencia de goles DESC, goles a favor DESC

Nota técnica: JSX no es compatible con el parser HTML de Thymeleaf (attoparser interpreta los spreads `{...obj}` como atributos HTML malformados). La solución fue separar el componente React en un archivo estático `.js` que Thymeleaf no procesa.

---

## Paginación de partidos

Los partidos de cada torneo se muestran paginados: 5 por página, ordenados por fecha descendente (más reciente primero).

- `MatchRepository`: `Page<Match> findByTournamentIdOrderByMatchDateDesc(Long id, Pageable pageable)`
- `TournamentService.findMatchesPaginated(Long tournamentId, int page, int size)`
- `TournamentController`: acepta `@RequestParam(defaultValue="0") int page`

---

## Búsqueda y filtros de jugadores

La página `/players` permite filtrar por nombre/apellido y por posición mediante un formulario GET.

- **JPQL con parámetros opcionales:** si el parámetro es `null`, la condición se omite con `IS NULL`
- **Posiciones disponibles:** Portero, Defensa, Centrocampista, Delantero
- **Reseteo de filtros:** botón "Limpiar filtros" visible solo cuando hay filtros activos

---

## Optimización N+1 (requisito SS.8)

### Problema

Al cargar el detalle de un partido, Hibernate lanzaba 7 queries separadas por lazy loading de cada relación (`homeTeam`, `awayTeam`, `tournament`, `referee`, comentarios).

Tiempo medido: ~7.5 ms solo en consultas.

### Solución: LEFT JOIN FETCH

```java
@Query("SELECT m FROM Match m " +
       "LEFT JOIN FETCH m.comments c " +
       "LEFT JOIN FETCH m.homeTeam " +
       "LEFT JOIN FETCH m.awayTeam " +
       "WHERE m.id = :id")
Optional<Match> findByIdWithComments(@Param("id") Long id);
```

`JOIN FETCH` obliga a Hibernate a generar un único `SELECT` con `JOIN`, cargando todas las relaciones en una sola consulta.

### Resultado

| Métrica | Sin optimización | Con JOIN FETCH |
|---------|-----------------|---------------|
| Queries JDBC | 7 | 3 |
| Tiempo total | ~7.5 ms | ~3.6 ms |
| Mejora | — | 52% mas rapido |

Análisis completo documentado en:
- `n+1/analisis_queries_pablo_rejon.md` (ES)
- `n+1/query_analysis_pablo_rejon_en.md` (EN)

---

## Validación de datos

Todas las entidades llevan anotaciones Bean Validation. Los controllers aplican `@Valid` + `BindingResult` y devuelven el formulario con mensajes de error si la validación falla.

| Entidad | Validaciones |
|---------|-------------|
| `Tournament` | `@NotBlank` + `@Size(3,100)` en name; `@NotBlank` + `@Size(max=1000)` en description; `@NotNull` en startDate |
| `Team` | `@NotBlank` en name y city; `@NotNull @Min(1800) @Max(2100)` en foundationYear |
| `Player` | `@NotBlank` en name, surname, position; `@NotNull @DecimalMin(1.40) @DecimalMax(2.20)` en height; `@NotNull` en birthDate |
| `Referee` | `@NotBlank` en name, surname; `@NotBlank @Size(3,20)` en refereeCode; unicidad verificada en controller |

---

## Gestión de errores

Spring Boot detecta automáticamente `templates/error.html` y la usa para todos los errores HTTP.

- **404** — "Página no encontrada"
- **403** — "Acceso denegado"
- **500** — "Error interno del servidor"

Para probar la vista de error:
- Navegar a una URL inexistente, p.ej. `http://localhost:8081/nonexistent` → 404
- Intentar acceder a `/admin/tournament/new` sin haber iniciado sesión como admin → 403

---

## Seguridad

| Configuración | Detalle |
|--------------|---------|
| Contraseñas | BCrypt (`BCryptPasswordEncoder`) |
| CSRF | Activo (protección por defecto Spring Security) |
| Rutas públicas | `/`, `/tournament/**`, `/team/**`, `/match/**`, `/players`, `/referees`, `/teams`, `/api/**`, `/css/**`, `/js/**`, `/images/**`, `/login`, `/register` |
| Autenticación requerida | `POST /match/*/comment` |
| Solo ADMIN | `/admin/**` |
| Roles | `ROLE_ADMIN`, `ROLE_USER` (prefijo `ROLE_` añadido por `CustomUserDetailsService`) |

---

## Estructura del proyecto

```
src/main/java/.../
├── config/
│   ├── SecurityConfig.java         -- Spring Security: roles, rutas, BCrypt, CSRF
│   └── DataInitializer.java        -- Seed: 3 torneos, 12 equipos, 44 jugadores, 35 partidos
├── controller/
│   ├── TournamentController.java   -- @Valid + partidos paginados
│   ├── TeamController.java         -- @Valid + BindingResult
│   ├── PlayerController.java       -- @Valid + búsqueda con filtros
│   ├── RefereeController.java      -- @Valid + unicidad refereeCode
│   ├── MatchController.java        -- comentarios + edición propia + borrado admin
│   ├── StandingsController.java    -- REST: GET /api/tournament/{id}/standings
│   └── AuthController.java         -- /login, /register
├── service/
│   ├── TournamentService.java      -- computeStandings() + findMatchesPaginated()
│   ├── TeamService.java            -- cascade delete: limpia jugadores y partidos
│   ├── PlayerService.java          -- search(q, position)
│   ├── MatchService.java           -- save + delete + findByIdWithComments
│   ├── CommentService.java         -- updateComment() con verificación de propiedad
│   ├── RefereeService.java
│   ├── UserService.java
│   └── CustomUserDetailsService.java -- UserDetailsService impl, añade prefijo ROLE_
├── repository/
│   ├── MatchRepository.java        -- JOIN FETCH + Page<Match>
│   ├── PlayerRepository.java       -- search @Query con parámetros opcionales
│   └── ...
└── model/
    ├── Tournament.java, Team.java, Player.java, Referee.java
    ├── Match.java, Comment.java, User.java, MatchStatus.java
    └── StandingEntry.java          -- Java record para serialización JSON

src/main/resources/
├── static/js/standings.js          -- React 18 (React.createElement, sin JSX)
├── templates/error.html            -- Página de error (404/403/500)
├── templates/comment-edit.html     -- Formulario edición comentario propio
├── templates/tournament-details.html -- Clasificación + partidos paginados
└── templates/players.html          -- Lista jugadores con búsqueda y filtros

n+1/
├── analisis_queries_pablo_rejon.md       -- Análisis N+1 (ES)
└── query_analysis_pablo_rejon_en.md      -- N+1 analysis (EN)

```

---

## Datos de demo

El `DataInitializer` se ejecuta al arrancar si la base de datos está vacía:

- **Torneos:** Champions League 2025/26, La Liga 2025/26, Serie A 2025/26
- **Equipos:** Real Madrid, FC Barcelona, Atlético, Sevilla, Man. City, Liverpool, PSG, Bayern, AS Roma, Inter, Juventus, Napoli
- **Jugadores:** ~44 con nombre, apellido, posición, fecha de nacimiento y altura
- **Árbitros:** 6 árbitros con código oficial
- **Partidos:** 35 distribuidos entre los 3 torneos (mix PLAYED/SCHEDULED con marcadores reales)
- **Usuarios:** admin/admin (ADMIN), pablo/1234 (USER)

---

## Changelog

| Fase | Descripción |
|------|------------|
| 1 | Setup Spring Boot + PostgreSQL Docker + dominio base |
| 2 | Entidades completas (Player, Referee, Match) + relaciones JPA |
| 3 | Presentación Thymeleaf + Spring Security + CRUD admin |
| 4 | ManyToMany Tournament-Team + limpieza de datos huérfanos |
| 5 | Campos obligatorios PDF: description, city, birthDate, height, refereeCode, status, location |
| 6 | Sistema de comentarios + vista de acta de partido |
| 7 | React 18 CDN + clasificación en tiempo real via REST API |
| 8 | Datos de demo expandidos (3 torneos, 12 equipos, 44 jugadores, 35 partidos) |
| 9 | Edición de comentarios propios + correcciones (@Transactional, unique constraint) |
| 10 | Validación Bean Validation en entidades + @Valid + BindingResult en controllers |
| 11 | Página de error personalizada (404/403/500) via templates/error.html |
| 12 | Paginación de partidos por torneo (5/pág, más reciente primero) |
| 13 | Búsqueda y filtros de jugadores por nombre/apellido y posición |

---

## Entrega

- **Destinatario:** siw.roma3@gmail.com
- **Asunto:** `[Giugno 2026 PROGETTO DOCENTE] Rejón Camacho 652799`
- **Contenido:** URL del repositorio GitHub + malfuncionamientos conocidos + consideraciones generales
- **Malfuncionamientos conocidos:** ninguno
