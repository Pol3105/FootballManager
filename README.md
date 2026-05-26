# SIW Football — Football Tournament Management

Mandatory project for **Sistemi Informativi su Web (SIW)** — Università degli Studi Roma Tre.

Full-stack web application for managing football tournaments: teams, players, referees, matches and live standings.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21, Spring Boot 3.4.5 |
| Persistence | Spring Data JPA, Hibernate, PostgreSQL 15 |
| Frontend | Thymeleaf 3.1.3, HTML5, CSS3 |
| Dynamic components | React 18 via CDN (no build step) |
| Security | Spring Security 6, BCrypt |
| Database | PostgreSQL in Docker (port 5435) |

---

## Architecture — 3 Layers

```
Presentation  ->  Controller  (HTTP, Thymeleaf, REST)
Business      ->  Service     (@Transactional, business logic)
Persistence   ->  Repository  (Spring Data JPA, JPQL)
```

Controllers never access repositories directly. All business logic lives in the service layer.

---

## Data Model

| Entity | Key fields |
|--------|-----------|
| `User` | username, password (BCrypt), role (ADMIN/USER) |
| `Tournament` | name, description, startDate |
| `Team` | name, foundationYear, city |
| `Player` | name, surname, position, birthDate, height, team |
| `Referee` | name, surname, refereeCode |
| `Match` | homeTeam, awayTeam, tournament, referee, homeScore, awayScore, status (SCHEDULED/PLAYED), location, matchDate |
| `Comment` | content, match, user |

### JPA Relationships

- `Tournament` 1:N `Match` — cascade ALL
- `Match` 1:N `Comment` — cascade ALL
- `Team` N:M `Tournament` — join table `team_tournament`
- `Player` N:1 `Team` — FetchType.LAZY
- `Match` N:1 `Team` (home/away) — explicit `@JoinColumn` to avoid column name conflict

All relationships use `FetchType.LAZY` by default to prevent the N+1 problem.

---

## How to Run

### 1. Database

```bash
docker-compose up -d
```

Starts PostgreSQL at `localhost:5435` with database `siw_football`.

### 2. Application

```bash
./mvnw spring-boot:run
```

Open: `http://localhost:8081`

### Default credentials

| Username | Password | Role |
|----------|---------|------|
| `admin` | `admin` | ADMIN |
| `pablo` | `1234` | USER |

### Reset demo data

```sql
TRUNCATE match, player, team_tournament, team, tournament, referee, users RESTART IDENTITY CASCADE;
```

Restart the application. `DataInitializer` inserts: 3 tournaments, 12 teams, ~44 players, 6 referees, 35 matches.

---

## Features

### Public access (no authentication required)

- Tournament list
- Tournament detail: participating teams, live standings, paginated match calendar
- Match detail: result, referee, stadium, comments
- Team list with squads
- Player list with search and filters
- Referee list

### Authenticated user (USER role)

- Post comments on matches
- Edit own comments (ownership check enforced in the service layer)

### Administrator (ADMIN role)

- Full CRUD for tournaments, teams, players, referees and matches
- Register match results
- Manage participating teams per tournament
- Delete any user's comments

---

## Standings — React 18 + REST API

The standings table is rendered with **React 18 via CDN** (no npm, webpack or compilation step).

- **REST endpoint:** `GET /api/tournament/{id}/standings` → JSON
- **Component:** `/static/js/standings.js` — pure `React.createElement()`, no JSX
- **Thymeleaf integration:** tournament ID injected as a global JS variable to avoid attoparser conflicts
- **Sort order:** points DESC, goal difference DESC, goals scored DESC

Technical note: JSX is incompatible with Thymeleaf's HTML parser (attoparser interprets `{...obj}` spreads as malformed HTML attributes). The solution was to move the React component into a static `.js` file that Thymeleaf does not process.

---

## Match Pagination

Each tournament's matches are paginated: 5 per page, sorted by date descending (most recent first).

- `MatchRepository`: `Page<Match> findByTournamentIdOrderByMatchDateDesc(Long id, Pageable pageable)`
- `TournamentService.findMatchesPaginated(Long tournamentId, int page, int size)`
- `TournamentController`: accepts `@RequestParam(defaultValue="0") int page`

---

## Player Search and Filters

The `/players` page allows filtering by name/surname and position via a GET form.

- **JPQL with optional parameters:** empty string sentinel (`= ''`) instead of null — avoids PostgreSQL `lower(bytea)` type inference error
- **Available positions:** Portero, Defensa, Centrocampista, Delantero
- **Clear filters:** button visible only when filters are active

---

## N+1 Query Optimisation (requirement §8)

### Problem

When loading a match detail page, Hibernate fired 7 separate queries due to lazy loading of each relationship (`homeTeam`, `awayTeam`, `tournament`, `referee`, comments).

Measured time: ~7.5 ms in queries alone.

### Solution: LEFT JOIN FETCH

```java
@Query("SELECT m FROM Match m " +
       "LEFT JOIN FETCH m.homeTeam " +
       "LEFT JOIN FETCH m.awayTeam " +
       "LEFT JOIN FETCH m.tournament " +
       "LEFT JOIN FETCH m.referee " +
       "WHERE m.id = :id")
Optional<Match> findByIdWithDetails(@Param("id") Long id);
```

`JOIN FETCH` forces Hibernate to generate a single `SELECT` with `JOIN`, loading all relationships in one query.

### Result

| Metric | Without optimisation | With JOIN FETCH |
|--------|---------------------|----------------|
| JDBC queries | 7 | 3 |
| Total time | ~7.5 ms | ~3.6 ms |
| Improvement | — | 52% faster |

Full analysis:
- `n+1/analisis_queries_pablo_rejon.md` (ES)
- `n+1/query_analysis_pablo_rejon_en.md` (EN)

---

## Data Validation

All entities carry Bean Validation annotations. Controllers apply `@Valid` + `BindingResult` and return the form with error messages on failure.

| Entity | Validations |
|--------|------------|
| `Tournament` | `@NotBlank` + `@Size(3,100)` on name; `@NotBlank` + `@Size(max=1000)` on description; `@NotNull` on startDate |
| `Team` | `@NotBlank` on name and city; `@NotNull @Min(1800) @Max(2100)` on foundationYear |
| `Player` | `@NotBlank` on name, surname, position; `@NotNull @DecimalMin(1.40) @DecimalMax(2.20)` on height; `@NotNull` on birthDate |
| `Referee` | `@NotBlank` on name, surname; `@NotBlank @Size(3,20)` on refereeCode; uniqueness checked programmatically in controller via `result.rejectValue()` |

---

## Error Handling

Spring Boot automatically detects `templates/error.html` and uses it for all HTTP errors.

- **404** — Page not found
- **403** — Access denied
- **500** — Internal server error

To test:
- Navigate to a non-existent URL, e.g. `http://localhost:8081/nonexistent` → 404
- Try to access `/admin/tournament/new` without admin session → 403

---

## Security

| Configuration | Detail |
|--------------|--------|
| Passwords | BCrypt (`BCryptPasswordEncoder`) |
| CSRF | Enabled (Spring Security default) |
| Public routes | `/`, `/tournament/**`, `/team/**`, `/match/**`, `/players`, `/referees`, `/teams`, `/api/**`, `/css/**`, `/js/**`, `/images/**`, `/login`, `/register` |
| Authentication required | `POST /match/*/comment` |
| ADMIN only | `/admin/**` |
| Roles | `ROLE_ADMIN`, `ROLE_USER` (`ROLE_` prefix added by `CustomUserDetailsService`) |

---

## Project Structure

```
src/main/java/.../
├── config/
│   ├── SecurityConfig.java             -- Spring Security: roles, routes, BCrypt, CSRF
│   └── DataInitializer.java            -- Seed: 3 tournaments, 12 teams, 44 players, 35 matches
├── controller/
│   ├── TournamentController.java       -- @Valid + paginated matches
│   ├── TeamController.java             -- @Valid + BindingResult
│   ├── PlayerController.java           -- @Valid + search with filters
│   ├── RefereeController.java          -- @Valid + refereeCode uniqueness check
│   ├── MatchController.java            -- comments + own edit + admin delete
│   ├── StandingsController.java        -- REST: GET /api/tournament/{id}/standings
│   └── AuthController.java             -- /login, /register
├── service/
│   ├── TournamentService.java          -- computeStandings() + findMatchesPaginated()
│   ├── TeamService.java                -- cascade delete: cleans players and matches
│   ├── PlayerService.java              -- search(q, position)
│   ├── MatchService.java               -- save + delete + findByIdWithComments
│   ├── CommentService.java             -- updateComment() with ownership check
│   ├── RefereeService.java
│   ├── UserService.java
│   └── CustomUserDetailsService.java   -- UserDetailsService impl, adds ROLE_ prefix
├── repository/
│   ├── MatchRepository.java            -- JOIN FETCH + Page<Match>
│   ├── PlayerRepository.java           -- search @Query with optional parameters
│   └── ...
└── model/
    ├── Tournament.java, Team.java, Player.java, Referee.java
    ├── Match.java, Comment.java, User.java, MatchStatus.java
    └── StandingEntry.java              -- Java record for JSON serialisation

src/main/resources/
├── static/js/standings.js              -- React 18 (React.createElement, no JSX)
├── templates/error.html                -- Custom error page (404/403/500)
├── templates/comment-edit.html         -- Own comment edit form
├── templates/tournament-details.html   -- Standings + paginated matches
└── templates/players.html              -- Player list with search and filters

n+1/
├── analisis_queries_pablo_rejon.md     -- N+1 analysis (ES)
└── query_analysis_pablo_rejon_en.md    -- N+1 analysis (EN)
```

---

## Demo Data

`DataInitializer` runs on startup if the database is empty:

- **Tournaments:** Champions League 2025/26, La Liga 2025/26, Serie A 2025/26
- **Teams:** Real Madrid, FC Barcelona, Atletico, Sevilla, Man. City, Liverpool, PSG, Bayern, AS Roma, Inter, Juventus, Napoli
- **Players:** ~44 with name, surname, position, date of birth and height
- **Referees:** 6 referees with official codes
- **Matches:** 35 distributed across 3 tournaments (mix of PLAYED/SCHEDULED with real scorelines)
- **Users:** admin/admin (ADMIN), pablo/1234 (USER)

---

## Changelog

| Phase | Description |
|-------|------------|
| 1 | Spring Boot + PostgreSQL Docker setup + base domain |
| 2 | Full entities (Player, Referee, Match) + JPA relationships |
| 3 | Thymeleaf views + Spring Security + admin CRUD |
| 4 | ManyToMany Tournament-Team + orphan data cleanup |
| 5 | Required PDF fields: description, city, birthDate, height, refereeCode, status, location |
| 6 | Comment system + match detail view |
| 7 | React 18 CDN + live standings via REST API |
| 8 | Expanded demo data (3 tournaments, 12 teams, 44 players, 35 matches) |
| 9 | Own comment editing + fixes (@Transactional, unique constraint) |
| 10 | Bean Validation on entities + @Valid + BindingResult in controllers |
| 11 | Custom error page (404/403/500) via templates/error.html |
| 12 | Match pagination per tournament (5/page, most recent first) |
| 13 | Player search and filters by name/surname and position |

---

## Submission

- **To:** siw.roma3@gmail.com
- **Subject:** `[Giugno 2026 PROGETTO DOCENTE] Rejón Camacho 652799`
- **Body:** GitHub repository URL + known malfunctions + general considerations
- **Known malfunctions:** none (a referee validation bug was discovered during a thorough QA audit and has been successfully resolved)

---

## Exhaustive QA & Solved Bugs

After a comprehensive Quality Assurance (QA) session testing all CRUD operations, user roles, security, and edge cases, we found and solved the following issue:

### 1. Referee Edit Validation Bug (Resolved)
- **Description:** When editing an existing referee, the database uniqueness check for the `refereeCode` was also scanning the record itself, resulting in a false-positive duplicate error ("Este código arbitral ya está asignado a otro árbitro") when saving the form without modifying the code.
- **Fix:**
  - Added `existsByRefereeCodeAndIdNot(String refereeCode, Long id)` to `RefereeRepository`.
  - Updated `RefereeController` to conditionally check uniqueness: utilizing the ID-excluding query on updates and the standard query on new entries.
  - This allows existing referee records to be successfully edited and saved with their original codes.

