# SIW Football — Gestione Tornei di Calcio

Progetto obbligatorio per **Sistemi Informativi su Web (SIW)** — Università degli Studi Roma Tre.

Applicazione web completa per la gestione di tornei di calcio: squadre, giocatori, arbitri, partite e classifiche in tempo reale.

---

## Stack tecnologico

| Livello | Tecnologia |
|---------|-----------|
| Backend | Java 21, Spring Boot 3.4.5 |
| Persistenza | Spring Data JPA, Hibernate, PostgreSQL 15 |
| Frontend | Thymeleaf 3.1.3, HTML5, CSS3 |
| Componenti dinamici | React 18 via CDN (senza build step) |
| Sicurezza | Spring Security 6, BCrypt |
| Database | PostgreSQL in Docker (porta 5435) |

---

## Architettura — 3 Livelli

```
Presentazione  ->  Controller  (HTTP, Thymeleaf, REST)
Business       ->  Service     (@Transactional, logica di business)
Persistenza    ->  Repository  (Spring Data JPA, JPQL)
```

I controller non accedono mai direttamente ai repository. Tutta la logica di business risiede nel service layer.

---

## Modello dei dati

| Entità | Campi principali |
|--------|-----------------|
| `User` | username, password (BCrypt), role (ADMIN/USER) |
| `Tournament` | name, description, startDate |
| `Team` | name, foundationYear, city |
| `Player` | name, surname, position, birthDate, height, team |
| `Referee` | name, surname, refereeCode |
| `Match` | homeTeam, awayTeam, tournament, referee, homeScore, awayScore, status (SCHEDULED/PLAYED), location, matchDate |
| `Comment` | content, match, user |

### Relazioni JPA

- `Tournament` 1:N `Match` — cascade ALL
- `Match` 1:N `Comment` — cascade ALL
- `Team` N:M `Tournament` — tabella intermedia `team_tournament`
- `Player` N:1 `Team` — FetchType.LAZY
- `Match` N:1 `Team` (home/away) — `@JoinColumn` esplicito per evitare conflitti sui nomi delle colonne

Tutte le relazioni usano `FetchType.LAZY` per impostazione predefinita per evitare il problema N+1.

---

## Come eseguire

### 1. Database

```bash
docker-compose up -d
```

Avvia PostgreSQL su `localhost:5435` con il database `siw_football`.

### 2. Applicazione

```bash
./mvnw spring-boot:run
```

Aprire: `http://localhost:8081`

### Credenziali predefinite

| Utente | Password | Ruolo |
|--------|---------|-------|
| `admin` | `admin` | ADMIN |
| `pablo` | `1234` | USER |

### Reset dei dati demo

```sql
TRUNCATE match, player, team_tournament, team, tournament, referee, users RESTART IDENTITY CASCADE;
```

Riavviare l'applicazione. `DataInitializer` inserisce: 3 tornei, 12 squadre, ~44 giocatori, 6 arbitri, 35 partite.

---

## Funzionalità

### Accesso pubblico (senza autenticazione)

- Lista tornei
- Dettaglio torneo: squadre partecipanti, classifica in tempo reale, calendario paginato
- Dettaglio partita: risultato, arbitro, stadio, commenti
- Lista squadre con rose
- Lista giocatori con ricerca e filtri
- Lista arbitri

### Utente autenticato (ruolo USER)

- Pubblicare commenti sulle partite
- Modificare i propri commenti (verifica di proprietà nel service layer)

### Amministratore (ruolo ADMIN)

- CRUD completo di tornei, squadre, giocatori, arbitri e partite
- Registrare risultati delle partite
- Gestire le squadre partecipanti per torneo
- Eliminare commenti di qualsiasi utente

---

## Classifica — React 18 + REST API

La tabella della classifica viene renderizzata con **React 18 via CDN** (senza npm, webpack o fase di compilazione).

- **Endpoint REST:** `GET /api/tournament/{id}/standings` → JSON
- **Componente:** `/static/js/standings.js` — usa `React.createElement()` puro, senza JSX
- **Integrazione Thymeleaf:** ID del torneo iniettato come variabile JS globale per evitare conflitti con attoparser
- **Ordinamento:** punti DESC, differenza reti DESC, gol fatti DESC

Nota tecnica: JSX non è compatibile con il parser HTML di Thymeleaf (attoparser interpreta gli spread `{...obj}` come attributi HTML malformati). La soluzione è stata spostare il componente React in un file `.js` statico che Thymeleaf non processa.

---

## Paginazione delle partite

Le partite di ogni torneo sono paginate: 5 per pagina, ordinate per data discendente (la più recente prima).

- `MatchRepository`: `Page<Match> findByTournamentIdOrderByMatchDateDesc(Long id, Pageable pageable)`
- `TournamentService.findMatchesPaginated(Long tournamentId, int page, int size)`
- `TournamentController`: accetta `@RequestParam(defaultValue="0") int page`

---

## Ricerca e filtri giocatori

La pagina `/players` permette di filtrare per nome/cognome e per posizione tramite un form GET.

- **JPQL con parametri opzionali:** stringa vuota come sentinella (`= ''`) invece di null — evita l'errore PostgreSQL `lower(bytea)`
- **Posizioni disponibili:** Portero, Defensa, Centrocampista, Delantero
- **Reset filtri:** pulsante visibile solo quando i filtri sono attivi

---

## Ottimizzazione N+1 (requisito §8)

### Problema

Caricando il dettaglio di una partita, Hibernate eseguiva 7 query separate per lazy loading di ogni relazione (`homeTeam`, `awayTeam`, `tournament`, `referee`, commenti).

Tempo misurato: ~7,5 ms solo nelle query.

### Soluzione: LEFT JOIN FETCH

```java
@Query("SELECT m FROM Match m " +
       "LEFT JOIN FETCH m.homeTeam " +
       "LEFT JOIN FETCH m.awayTeam " +
       "LEFT JOIN FETCH m.tournament " +
       "LEFT JOIN FETCH m.referee " +
       "WHERE m.id = :id")
Optional<Match> findByIdWithDetails(@Param("id") Long id);
```

`JOIN FETCH` obbliga Hibernate a generare un unico `SELECT` con `JOIN`, caricando tutte le relazioni in una sola query.

### Risultato

| Metrica | Senza ottimizzazione | Con JOIN FETCH |
|---------|---------------------|---------------|
| Query JDBC | 7 | 3 |
| Tempo totale | ~7,5 ms | ~3,6 ms |
| Miglioramento | — | 52% più veloce |

Analisi completa:
- `n+1/analisis_queries_pablo_rejon.md` (ES)
- `n+1/query_analysis_pablo_rejon_en.md` (EN)

---

## Validazione dei dati

Tutte le entità portano annotazioni Bean Validation. I controller applicano `@Valid` + `BindingResult` e restituiscono il form con messaggi di errore in caso di fallimento.

| Entità | Validazioni |
|--------|------------|
| `Tournament` | `@NotBlank` + `@Size(3,100)` su name; `@NotBlank` + `@Size(max=1000)` su description; `@NotNull` su startDate |
| `Team` | `@NotBlank` su name e city; `@NotNull @Min(1800) @Max(2100)` su foundationYear |
| `Player` | `@NotBlank` su name, surname, position; `@NotNull @DecimalMin(1.40) @DecimalMax(2.20)` su height; `@NotNull` su birthDate |
| `Referee` | `@NotBlank` su name, surname; `@NotBlank @Size(3,20)` su refereeCode; unicità verificata programmaticamente nel controller via `result.rejectValue()` |

---

## Gestione degli errori

Spring Boot rileva automaticamente `templates/error.html` e la usa per tutti gli errori HTTP.

- **404** — Pagina non trovata
- **403** — Accesso negato
- **500** — Errore interno del server

Per testare:
- Navigare a un URL inesistente, es. `http://localhost:8081/nonexistent` → 404
- Tentare di accedere a `/admin/tournament/new` senza sessione admin → 403

---

## Sicurezza

| Configurazione | Dettaglio |
|---------------|----------|
| Password | BCrypt (`BCryptPasswordEncoder`) |
| CSRF | Attivo (protezione predefinita Spring Security) |
| Route pubbliche | `/`, `/tournament/**`, `/team/**`, `/match/**`, `/players`, `/referees`, `/teams`, `/api/**`, `/css/**`, `/js/**`, `/images/**`, `/login`, `/register` |
| Autenticazione richiesta | `POST /match/*/comment` |
| Solo ADMIN | `/admin/**` |
| Ruoli | `ROLE_ADMIN`, `ROLE_USER` (prefisso `ROLE_` aggiunto da `CustomUserDetailsService`) |

---

## Struttura del progetto

```
src/main/java/.../
├── config/
│   ├── SecurityConfig.java             -- Spring Security: ruoli, route, BCrypt, CSRF
│   └── DataInitializer.java            -- Seed: 3 tornei, 12 squadre, 44 giocatori, 35 partite
├── controller/
│   ├── TournamentController.java       -- @Valid + partite paginate
│   ├── TeamController.java             -- @Valid + BindingResult
│   ├── PlayerController.java           -- @Valid + ricerca con filtri
│   ├── RefereeController.java          -- @Valid + verifica unicità refereeCode
│   ├── MatchController.java            -- commenti + modifica propria + eliminazione admin
│   ├── StandingsController.java        -- REST: GET /api/tournament/{id}/standings
│   └── AuthController.java             -- /login, /register
├── service/
│   ├── TournamentService.java          -- computeStandings() + findMatchesPaginated()
│   ├── TeamService.java                -- cascade delete: pulisce giocatori e partite
│   ├── PlayerService.java              -- search(q, position)
│   ├── MatchService.java               -- save + delete + findByIdWithComments
│   ├── CommentService.java             -- updateComment() con verifica proprietà
│   ├── RefereeService.java
│   ├── UserService.java
│   └── CustomUserDetailsService.java   -- UserDetailsService impl, aggiunge prefisso ROLE_
├── repository/
│   ├── MatchRepository.java            -- JOIN FETCH + Page<Match>
│   ├── PlayerRepository.java           -- search @Query con parametri opzionali
│   └── ...
└── model/
    ├── Tournament.java, Team.java, Player.java, Referee.java
    ├── Match.java, Comment.java, User.java, MatchStatus.java
    └── StandingEntry.java              -- Java record per serializzazione JSON

src/main/resources/
├── static/js/standings.js              -- React 18 (React.createElement, senza JSX)
├── templates/error.html                -- Pagina di errore personalizzata (404/403/500)
├── templates/comment-edit.html         -- Form modifica commento proprio
├── templates/tournament-details.html   -- Classifica + partite paginate
└── templates/players.html              -- Lista giocatori con ricerca e filtri

n+1/
├── analisis_queries_pablo_rejon.md     -- Analisi N+1 (ES)
└── query_analysis_pablo_rejon_en.md    -- N+1 analysis (EN)
```

---

## Dati demo

`DataInitializer` viene eseguito all'avvio se il database è vuoto:

- **Tornei:** Champions League 2025/26, La Liga 2025/26, Serie A 2025/26
- **Squadre:** Real Madrid, FC Barcelona, Atletico, Siviglia, Man. City, Liverpool, PSG, Bayern, AS Roma, Inter, Juventus, Napoli
- **Giocatori:** ~44 con nome, cognome, posizione, data di nascita e altezza
- **Arbitri:** 6 arbitri con codice ufficiale
- **Partite:** 35 distribuite tra i 3 tornei (mix PLAYED/SCHEDULED con risultati reali)
- **Utenti:** admin/admin (ADMIN), pablo/1234 (USER)

---

## Changelog

| Fase | Descrizione |
|------|------------|
| 1 | Setup Spring Boot + PostgreSQL Docker + dominio base |
| 2 | Entità complete (Player, Referee, Match) + relazioni JPA |
| 3 | Viste Thymeleaf + Spring Security + CRUD admin |
| 4 | ManyToMany Tournament-Team + pulizia dati orfani |
| 5 | Campi obbligatori PDF: description, city, birthDate, height, refereeCode, status, location |
| 6 | Sistema di commenti + vista dettaglio partita |
| 7 | React 18 CDN + classifica in tempo reale via REST API |
| 8 | Dati demo espansi (3 tornei, 12 squadre, 44 giocatori, 35 partite) |
| 9 | Modifica commenti propri + correzioni (@Transactional, unique constraint) |
| 10 | Bean Validation sulle entità + @Valid + BindingResult nei controller |
| 11 | Pagina di errore personalizzata (404/403/500) via templates/error.html |
| 12 | Paginazione partite per torneo (5/pagina, la più recente prima) |
| 13 | Ricerca e filtri giocatori per nome/cognome e posizione |

---

## Consegna

- **Destinatario:** siw.roma3@gmail.com
- **Oggetto:** `[Giugno 2026 PROGETTO DOCENTE] Rejón Camacho 652799`
- **Contenuto:** URL repository GitHub + malfunzionamenti noti + considerazioni generali
- **Malfunzionamenti noti:** nessuno
