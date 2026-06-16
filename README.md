# SIW Football — Football Tournament Management

Mandatory project for **Sistemi Informativi su Web (SIW)** — Università degli Studi Roma Tre.

Full-stack web application for managing football tournaments: teams, players, referees, matches and live standings.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21, Spring Boot 3.4.5 |
| Persistence | Spring Data JPA, Hibernate, PostgreSQL 15 |
| Frontend | Thymeleaf 3.1.3, HTML5, CSS3 (custom-property theming) |
| Dynamic components | React 18 via CDN (standings) + vanilla JS (dock, modals, dot-map, fancy-select, carousel) |
| Security | Spring Security 6, BCrypt, OAuth2 Login (Google) |
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
| `User` | username, password (BCrypt, nullable for OAuth users), role (ADMIN/USER), provider (LOCAL/GOOGLE) |
| `Tournament` | name, description, startDate |
| `Team` | name, foundationYear, city |
| `Player` | name, surname, position, birthDate, height, team |
| `Referee` | name, surname, refereeCode |
| `Match` | homeTeam, awayTeam, tournament, referee, homeScore, awayScore, status (SCHEDULED/PLAYED), location, matchDate |
| `Comment` | content, match, user |

### JPA Relationships

- `Tournament` 1:N `Match` — cascade ALL
- `Match` 1:N `Comment` — cascade ALL
- `Team` N:M `Tournament` — join table `team_tournament`; **`Team` is the owning side**
  (`Tournament.teams` is `mappedBy`), so writes from the tournament side go through
  `TournamentService.saveWithTeams(...)`
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

- Full CRUD for tournaments, teams, players, referees and matches (via dialog modals)
- Register match results (with same-team and date-vs-status validation)
- Manage participating teams per tournament — from the dedicated page **or** the team selector
  inside the create/edit tournament modal
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
| Passwords | BCrypt (`BCryptPasswordEncoder`); `null` for Google accounts |
| CSRF | Enabled (Spring Security default) |
| Login methods | Form login (username/password) **and** OAuth2 Login with Google |
| Public routes | `/`, `/tournament/**`, `/team/**`, `/match/**`, `/players`, `/referees`, `/teams`, `/api/**`, `/css/**`, `/js/**`, `/images/**`, `/login`, `/register`, `/oauth2/**`, `/login/oauth2/**` |
| Authentication required | `POST /match/*/comment` |
| ADMIN only | `/admin/**` |
| Roles | `ROLE_ADMIN`, `ROLE_USER` (`ROLE_` prefix added by `CustomUserDetailsService` / `CustomOAuth2UserService`) |

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
├── static/js/standings.js              -- React 18 (React.createElement, no JSX), themed table
├── static/js/modal.js                  -- Modal open/close, edit-prefill, match validation
├── static/js/fancy-select.js           -- Custom themed <select> dropdowns
├── static/js/comments-carousel.js      -- Auto-rotating comments carousel
├── static/css/{modal,fancy-select,match-details,dock,footer}.css
├── templates/fragments.html            -- Shared fragments (dock, footer, modals)
├── templates/error.html                -- Custom error page (404/403/500)
├── templates/comment-edit.html         -- Own comment edit form
├── templates/tournament-details.html   -- Themed standings + match cards + match/tournament modals
├── templates/match-details.html        -- Scoreboard + comments carousel + edit/comment modals
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

## UI/UX Redesign (vps-deploy branch)

A full visual overhaul, all done in Thymeleaf + plain CSS + vanilla JS (the source
design references were React/shadcn components, re-implemented natively — no build step,
no React except the existing standings widget).

### Theme & branding
- **Warm brown/cream theme** driven by CSS custom properties in `static/css/variables.css`
  (`--background`, `--foreground`, `--primary #644a40`, `--secondary`, `--muted`, `--border`,
  `--ring`, …) with a ready `.dark` token block. Legacy `--color-*` tokens are remapped to the
  new palette so every existing page restyles without rewriting each stylesheet.
- Rebranded to **"Football Manager"** with a `logo.webp` asset.
- PWA `theme_color` updated to `#644a40` + `<meta name="theme-color">` on every page (fixes
  Safari tinting the toolbar green).

### Authentication pages
- **Login** redesigned as a glass card (icon inputs, password show/hide toggle in
  `static/js/auth.js`, "remember me", arrow CTA).
- **Register** as a split card with an animated **dot-map canvas** (`static/js/dotmap.js`)
  on the left panel; keeps the math anti-bot challenge and validation.
- **OAuth2 Login with Google** button on both pages → `/oauth2/authorization/google`.

### OAuth2 Google login (backend)
- Dependency `spring-boot-starter-oauth2-client`; registration configured in
  `application.properties` from env vars `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET`.
- `CustomOAuth2UserService` upserts the user by Google email (creates it with role `USER`
  and `provider = GOOGLE`, no local password) and grants `ROLE_USER`.
- `User.provider` column (`LOCAL` / `GOOGLE`, default `LOCAL` with a safe column default so
  Hibernate can backfill existing rows); `password` is now nullable.
- `SecurityConfig` adds `.oauth2Login(...)` wired to the custom service, form login untouched.

### Site-wide UI
- **Ethereal animated background** (`static/css/etheral-shadow.css`) on every page — a soft
  blurred, drifting masked shape + noise. The original heavy SVG `feDisplacementMap` filter
  was removed because Safari/WebKit computes it on the CPU full-screen and stutters; replaced
  by a GPU-cheap `blur` + `transform` drift, smooth on all browsers.
- **macOS-style dock** (`static/css/dock.css`, `static/js/dock.js`): fixed bottom nav with a
  magnify-on-hover effect (Jugadores / Árbitros / Equipos). For admins it shows a white **"+"**
  item that is **context-aware** — it opens the right creation modal for the current page
  (new tournament on home, new team on `/teams`, new referee, new player, schedule match,
  new comment, …).
- **Footer** (`static/css/footer.css`): glassy footer with brand, navigation, account and info
  columns, site-wide.
- **Tournament cards** rebuilt: whole card is clickable, brand-gradient banner with team count,
  and the description **expands on hover** (CSS only).
- **Blur-fade entrance animation** on the home page (staggered fade + blur-in).

### CRUD via modals (no full-page redirects)
- Admin create/edit flows now happen in **dialog modals** instead of navigating to separate
  form pages: tournament, team, referee, player, match and comment modals
  (`static/css/modal.css`, `static/js/modal.js`). The JS handles open/close (click, overlay,
  Esc), edit-prefill from `data-*` attributes, and a return-URL so the user lands back where
  they were after saving. Form POSTs reuse the existing admin controller endpoints.
- **Custom select** dropdowns (`static/css/fancy-select.css`, `static/js/fancy-select.js`) and
  a **comments carousel** (`static/js/comments-carousel.js`).
- **Match scheduling/editing modal** with client-side validation (`static/js/modal.js`):
  blocks same home/away team and enforces date-vs-status rules (SCHEDULED → future date,
  PLAYED → past date), showing an inline `.modal-warning` instead of a server redirect. The
  `datetime-local` field is auto-filled on edit (ISO value trimmed to `yyyy-MM-ddTHH:mm`).
- **Tournament modal team selector:** a checkbox grid-list (`name="teams"`) lets the admin pick
  the participating teams when creating or editing a tournament. Because `Team.tournaments` is
  the owning side of the N:M relation, `TournamentService.saveWithTeams(...)` reconciles each
  team's side instead of relying on the inverse `Tournament.teams`.
- **Per-row "Opciones" dropdowns** (Edit / Delete-on-hold) on tournament header, match cards and
  comments, matching the team/player/referee cards. Delete uses the hold-to-confirm button.

### Tournament & match detail redesign
- **Tournament detail** (`templates/tournament-details.html`): removed the redundant "teams"
  badge strip (teams already appear in the standings), restyled the **standings table**
  (`static/js/standings.js`) to the theme (muted header, hover rows, numeric ranks — no medal
  emojis), and rebuilt the **match calendar as elongated, full-width clickable cards** (click →
  match detail). Match pagination moved to the **dock arrows**; the dock **"+"** opens the
  schedule-match modal.
- **Match detail** (`templates/match-details.html`, `static/css/match-details.css`): polished
  scoreboard (theme accent, status badges), and the comments are shown as an **auto-rotating
  testimonial carousel** (default user-icon avatar, no star ratings, prev/next + dots).
- **Emoji sweep:** all decorative emojis removed across views (kept only the error-page glyph).

### Responsive (mobile & tablet)
- Site-wide responsive pass: modal forms collapse 2-column grids to 1 column (`.form-grid-2`),
  modals get `max-height`/scroll on small screens, tables scroll horizontally, the 3D card-tilt
  is disabled on touch widths, and navbar/dock keep their touch targets (`global.css`,
  `modal.css`, `navbar.css`, `dock.css`).

### Shared Thymeleaf fragments
- `templates/fragments.html` defines reusable fragments (`ethereal`, `dock`, `footer`, and the
  modal blocks) included via `th:replace` across all non-auth pages — no per-page duplication.
- Global styles are wired through `@import` in `global.css`, so a single `<head>` link set
  applies the new components everywhere.

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
| 14 | Warm brown/cream theme via CSS custom properties + "Football Manager" rebrand |
| 15 | Auth redesign (login glass card, register split + dot-map canvas) + ethereal background |
| 16 | OAuth2 Login with Google (CustomOAuth2UserService, `User.provider`, nullable password) |
| 17 | macOS dock, glassy footer, redesigned tournament cards, blur-fade home animation |
| 18 | CRUD via dialog modals (tournament/team/referee/player/match/comment) + fancy-select; Safari background perf fix |
| 19 | Tournament & match detail redesign: themed standings table, elongated clickable match cards, dock-arrow match pagination, polished scoreboard, auto-rotating comments carousel; site-wide emoji removal |
| 20 | Match scheduling/editing as a modal with client-side validation (same-team + date-vs-status) and auto-filled date; per-row "Opciones" dropdowns on tournament/match/comments; match save return-URL |
| 21 | Tournament create/edit team selector (checkbox grid-list) with owning-side reconciliation in `TournamentService.saveWithTeams`; full responsive (mobile/tablet) pass; team cards recolored |

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

---

## VPS Hetzner Deployment (vps-deploy branch)

This project is configured to be deployed automatically to a Hetzner VPS (IP: `178.105.2.24`) using a fully Dockerized setup to avoid port collisions and maintain high security.

### 1. Port Configuration & Isolation (.env)
We run the entire application (monolithic Spring Boot + PostgreSQL) in Docker:
- Create a `.env` file in the root of your project `/home/pablo/apps/uni/FootballManager/.env` with your database credentials.
- **Configurable Credentials:** You can set secure production passwords for default users in the `.env` file:
  ```env
  ADMIN_PASSWORD=your_secure_admin_password
  USER_PASSWORD=your_secure_pablo_password
  ```
  On container startup, Spring Boot automatically updates the password hashes in the database using these variables.
- **Google OAuth credentials:** to enable "Sign in with Google", add to the same `.env`:
  ```env
  GOOGLE_CLIENT_ID=your_client_id
  GOOGLE_CLIENT_SECRET=your_client_secret
  ```
  Create them in Google Cloud Console (OAuth consent screen + Web application credentials) and
  register the redirect URIs `https://YOUR_DOMAIN/login/oauth2/code/google` (and
  `http://localhost:8081/login/oauth2/code/google` for local testing). The `.env` is gitignored,
  so the real secret lives only on the VPS. Form login keeps working even if these are unset.
- **Security:** Ports are mapped only to `127.0.0.1` (`127.0.0.1:8081:8081` for the app and `127.0.0.1:5435:5432` for PostgreSQL) to prevent direct external access.
- **Spring Boot Multi-stage Build:** The app compiles and runs inside Docker using a multi-stage `Dockerfile`.

### 2. Caddy Reverse Proxy (Docker Network Integration)
Since Caddy is running in Docker (container `caddy-caddy-1`) on the VPS, both Caddy and the application container (`siw_football_app`) join the shared external docker network `proxy-network`.
Add the following block to your Caddyfile:
```caddy
uni.pablo-server.178.105.2.24.sslip.io {
    reverse_proxy siw_football_app:8081
}
```

### 3. PWA & macOS "Add to Dock" Integration
We added support for Progressive Web Apps (PWA) and macOS standalone web apps:
- Icons (favicon, Apple touch icons) and `site.webmanifest` are located in `src/main/resources/static/`.
- The manifest is configured with the app name `SIW Football Manager` and the brown brand color (`theme_color #644a40`, matching `<meta name="theme-color">` on every page).
- **Security Config:** Spring Security (`SecurityConfig.java`) permits public access to `/favicon.ico`, `/*.png`, `/site.webmanifest`, and `/about.txt` so they can be loaded by browsers/macOS without auth redirects.
- In Safari on macOS, choose **File > Add to Dock...** to install the application with a high-resolution soccer ball icon.

### 4. Anti-Bot Register Spam Protection (Math Challenge + fail2ban)
To prevent malicious bots from spamming the database with registration queries:
- **Math Verification:** Added a math challenge (*"Verificación humana: ¿Cuánto es 5 + 3?"*) to the `/register` form. The backend verifies the answer is exactly `8` before executing database queries or password encoding.
- **fail2ban Protection:** Added a jail `caddy-register` on the host to monitor Caddy logs and block IPs that make excessive POST requests to `/register` (more than 5 attempts per minute).

### 5. CI/CD with GitHub Actions
Pushing to the `vps-deploy` branch triggers `.github/workflows/deploy.yml` which logs into the VPS via SSH, fetches changes, and runs:
```bash
docker compose down
docker compose up -d --build
```

### 6. Secure Database Management (TablePlus / SSH Tunnel)
Since PostgreSQL is bound strictly to `127.0.0.1:5435` on the VPS host, you can connect to it visually and securely from your Mac using **TablePlus** via an **SSH Tunnel**:
- **Connection Details (PostgreSQL):**
  - **Host:** `127.0.0.1` (refers to the localhost of the VPS itself once the tunnel is active)
  - **Port:** `5435`
  - **User / Password / Database:** As defined in your `.env` configuration file.
- **SSH Tunnel Settings:**
  - Click the **Over SSH** button in TablePlus.
  - **SSH Host:** `178.105.2.24` (VPS IP)
  - **Port:** `22`
  - **SSH User:** `pablo`
  - **Use SSH Key:** Select your private key file from your Mac (e.g. `~/.ssh/id_rsa` or `~/.ssh/id_ed25519`).
  - Click **Test** and then **Connect**.

