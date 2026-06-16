# SIW Football — Gestione Tornei di Calcio

Progetto obbligatorio per **Sistemi Informativi su Web (SIW)** — Università degli Studi Roma Tre.

Applicazione web completa per la gestione di tornei di calcio: squadre, giocatori, arbitri, partite e classifiche in tempo reale.

[![Guarda il Video di Presentazione](https://img.shields.io/badge/Video_di_Presentazione-Guarda_su_YouTube-red?style=for-the-badge&logo=youtube)](https://youtu.be/re42CX0M8ec)

---

## Stack tecnologico

| Livello | Tecnologia |
|---------|-----------|
| Backend | Java 21, Spring Boot 3.4.5 |
| Persistenza | Spring Data JPA, Hibernate, PostgreSQL 15 |
| Frontend | Thymeleaf 3.1.3, HTML5, CSS3 (theming con custom property) |
| Componenti dinamici | React 18 via CDN (classifica) + JS vanilla (dock, modali, dot-map, fancy-select, carosello) |
| Sicurezza | Spring Security 6, BCrypt, OAuth2 Login (Google) |
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
| `User` | username, password (BCrypt, nullable per utenti OAuth), role (ADMIN/USER), provider (LOCAL/GOOGLE) |
| `Tournament` | name, description, startDate |
| `Team` | name, foundationYear, city |
| `Player` | name, surname, position, birthDate, height, team |
| `Referee` | name, surname, refereeCode |
| `Match` | homeTeam, awayTeam, tournament, referee, homeScore, awayScore, status (SCHEDULED/PLAYED), location, matchDate |
| `Comment` | content, match, user |

### Relazioni JPA

- `Tournament` 1:N `Match` — cascade ALL
- `Match` 1:N `Comment` — cascade ALL
- `Team` N:M `Tournament` — tabella intermedia `team_tournament`; **`Team` è il lato proprietario**
  (`Tournament.teams` è `mappedBy`), quindi le scritture dal lato torneo passano per
  `TournamentService.saveWithTeams(...)`
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

- CRUD completo di tornei, squadre, giocatori, arbitri e partite (tramite modali dialog)
- Registrare risultati delle partite (con validazione stessa-squadra e data-vs-stato)
- Gestire le squadre partecipanti per torneo — dalla pagina dedicata **oppure** dal selettore
  di squadre dentro la modale di creazione/modifica torneo
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
| Password | BCrypt (`BCryptPasswordEncoder`); `null` per gli account Google |
| CSRF | Attivo (protezione predefinita Spring Security) |
| Metodi di login | Form login (username/password) **e** OAuth2 Login con Google |
| Route pubbliche | `/`, `/tournament/**`, `/team/**`, `/match/**`, `/players`, `/referees`, `/teams`, `/api/**`, `/css/**`, `/js/**`, `/images/**`, `/login`, `/register`, `/oauth2/**`, `/login/oauth2/**` |
| Autenticazione richiesta | `POST /match/*/comment` |
| Solo ADMIN | `/admin/**` |
| Ruoli | `ROLE_ADMIN`, `ROLE_USER` (prefisso `ROLE_` aggiunto da `CustomUserDetailsService` / `CustomOAuth2UserService`) |

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

## Restyling UI/UX (branch vps-deploy)

Rinnovamento visivo completo, realizzato in Thymeleaf + CSS puro + JS vanilla (i riferimenti
di design erano componenti React/shadcn, re-implementati nativamente — nessun build step,
nessun React tranne il widget classifica esistente).

### Tema & branding
- **Tema marrone/crema caldo** guidato da CSS custom property in `static/css/variables.css`
  (`--background`, `--foreground`, `--primary #644a40`, `--secondary`, `--muted`, `--border`,
  `--ring`, …) con un blocco di token `.dark` pronto. I vecchi token `--color-*` sono rimappati
  alla nuova palette, così ogni pagina cambia stile senza riscrivere ogni foglio di stile.
- Rebranding in **"Football Manager"** con asset `logo.webp`.
- `theme_color` PWA aggiornato a `#644a40` + `<meta name="theme-color">` su ogni pagina
  (risolve la barra di Safari tinta di verde).

### Pagine di autenticazione
- **Login** ridisegnato come glass card (input con icone, toggle mostra/nascondi password in
  `static/js/auth.js`, "ricordami", CTA con freccia).
- **Registrazione** come split card con una **canvas dot-map animata** (`static/js/dotmap.js`)
  nel pannello sinistro; mantiene la sfida anti-bot matematica e la validazione.
- Pulsante **OAuth2 Login con Google** su entrambe le pagine → `/oauth2/authorization/google`.

### OAuth2 Login con Google (backend)
- Dipendenza `spring-boot-starter-oauth2-client`; registrazione configurata in
  `application.properties` dalle variabili d'ambiente `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET`.
- `CustomOAuth2UserService` esegue l'upsert dell'utente tramite l'email Google (lo crea con
  ruolo `USER` e `provider = GOOGLE`, senza password locale) e assegna `ROLE_USER`.
- Colonna `User.provider` (`LOCAL` / `GOOGLE`, default `LOCAL` con un default di colonna sicuro
  per il backfill delle righe esistenti da parte di Hibernate); `password` ora è nullable.
- `SecurityConfig` aggiunge `.oauth2Login(...)` collegato al service custom, il form login resta intatto.

### UI globale
- **Sfondo animato "ethereal"** (`static/css/etheral-shadow.css`) su ogni pagina — una forma
  mascherata sfocata che deriva lentamente + rumore. Il pesante filtro SVG `feDisplacementMap`
  originale è stato rimosso perché Safari/WebKit lo calcola sulla CPU a schermo intero e scatta;
  sostituito da `blur` + deriva `transform` (leggeri per la GPU), fluido su tutti i browser.
- **Dock in stile macOS** (`static/css/dock.css`, `static/js/dock.js`): navigazione fissa in
  basso con effetto magnify all'hover (Giocatori / Arbitri / Squadre). Per gli admin mostra un
  item **"+"** bianco **contestuale** — apre la modale di creazione giusta per la pagina corrente
  (nuovo torneo in home, nuova squadra su `/teams`, nuovo arbitro, nuovo giocatore, programma
  partita, nuovo commento, …).
- **Footer** (`static/css/footer.css`): footer glass con colonne brand, navigazione, account e info,
  su tutto il sito.
- **Card dei tornei** ricostruite: l'intera card è cliccabile, banner a gradiente del brand con
  numero di squadre, e la descrizione **si espande all'hover** (solo CSS).
- **Animazione d'ingresso blur-fade** nella home (fade + blur scaglionati).

### CRUD tramite modali (niente redirect a pagina intera)
- I flussi admin di creazione/modifica avvengono ora in **modali dialog** invece di navigare a
  pagine separate: modali per torneo, squadra, arbitro, giocatore, partita e commento
  (`static/css/modal.css`, `static/js/modal.js`). Il JS gestisce apertura/chiusura (click,
  overlay, Esc), il prefill in modifica dai `data-*` e una return-URL per tornare dove si era
  dopo il salvataggio. I POST riusano gli endpoint admin esistenti.
- **Select personalizzate** (`static/css/fancy-select.css`, `static/js/fancy-select.js`) e un
  **carosello di commenti** (`static/js/comments-carousel.js`).

### Fragment Thymeleaf condivisi
- `templates/fragments.html` definisce fragment riutilizzabili (`ethereal`, `dock`, `footer` e i
  blocchi modali) inclusi via `th:replace` in tutte le pagine non-auth — nessuna duplicazione.
- Gli stili globali sono collegati via `@import` in `global.css`, così un solo set di link nel
  `<head>` applica i nuovi componenti ovunque.

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
| 14 | Tema marrone/crema con CSS custom property + rebrand "Football Manager" |
| 15 | Restyling auth (login glass card, registrazione split + canvas dot-map) + sfondo ethereal |
| 16 | OAuth2 Login con Google (CustomOAuth2UserService, `User.provider`, password nullable) |
| 17 | Dock macOS, footer glass, card tornei ridisegnate, animazione blur-fade in home |
| 18 | CRUD tramite modali (torneo/squadra/arbitro/giocatore/partita/commento) + fancy-select; fix performance sfondo su Safari |
| 19 | Riprogettazione dei dettagli del torneo e della partita: tabella della classifica a tema, card delle partite allungate e cliccabili, paginazione delle partite con frecce integrate nel dock, tabellone dei punteggi rifinito, carosello dei commenti a rotazione automatica; rimozione globale delle emoji |
| 20 | Programmazione/modifica della partita come modale con validazione lato client (stessa squadra + data vs stato) e data precompilata; dropdown "Opciones" per riga su torneo/partita/commenti; return-URL al salvataggio della partita |
| 21 | Selettore delle squadre per la creazione/modifica del torneo (griglia di checkbox) con riconciliazione del lato proprietario in `TournamentService.saveWithTeams`; adattamento responsive completo (mobile/tablet); ricolorazione delle card delle squadre |

---

## Consegna

- **Destinatario:** siw.roma3@gmail.com
- **Oggetto:** `[Giugno 2026 PROGETTO DOCENTE] Rejón Camacho 652799`
- **Contenuto:** URL repository GitHub + malfunzionamenti noti + considerazioni generali
- **Malfunzionamenti noti:** nessuno (un bug di validazione sulla modifica dell'arbitro è stato rilevato durante un QA approfondito ed è stato risolto con successo)

---

## QA Approfondito & Bug Risolti

A seguito di una sessione completa di Quality Assurance (QA) per testare tutte le operazioni CRUD, i ruoli utente, la sicurezza ed i casi limite, abbiamo riscontrato e risolto il seguente problema:

### 1. Bug di validazione nella modifica dell'arbitro (Risolto)
- **Descrizione:** Durante la modifica di un arbitro esistente, il controllo di unicità del `refereeCode` nel database includeva erroneamente anche il record stesso dell'arbitro in fase di modifica. Di conseguenza, il salvataggio lanciava un falso errore di codice duplicato ("Este código arbitral ya está asignado a otro árbitro") se il codice non veniva modificato.
- **Soluzione:**
  - Aggiunto il metodo `existsByRefereeCodeAndIdNot(String refereeCode, Long id)` nel `RefereeRepository`.
  - Aggiornato `RefereeController` per eseguire la verifica di unicità escludendo l'ID dell'arbitro corrente in fase di modifica, ed eseguendo la query standard solo per le nuove creazioni.
  - Questo permette di modificare e salvare correttamente i record degli arbitri esistenti mantenendo il proprio codice originale.

---

## Distribuzione su VPS Hetzner (vps-deploy branch)

Questo progetto è configurato per essere distribuito automaticamente su un VPS Hetzner (IP: `178.105.2.24`) utilizzando una configurazione completamente Dockerizzata per evitare collisioni di porte e garantire un'elevata sicurezza.

### 1. Configurazione Porte & Isolamento (.env)
Eseguiamo l'intera applicazione (Spring Boot monolitico + PostgreSQL) in Docker:
- Crea un file `.env` nella radice del progetto `/home/pablo/apps/uni/FootballManager/.env` con le credenziali del database.
- **Credenziali Configurabili:** Puoi definire password di produzione sicure per gli utenti predefiniti nel file `.env`:
  ```env
  ADMIN_PASSWORD=la_tua_password_admin_sicura
  USER_PASSWORD=la_tua_password_pablo_sicura
  ```
  All'avvio del container, Spring Boot aggiorna automaticamente gli hash delle password nel database con queste variabili.
- **Credenziali Google OAuth:** per abilitare "Accedi con Google", aggiungi allo stesso `.env`:
  ```env
  GOOGLE_CLIENT_ID=il_tuo_client_id
  GOOGLE_CLIENT_SECRET=il_tuo_client_secret
  ```
  Creale nella Google Cloud Console (schermata di consenso OAuth + credenziali Web application) e
  registra le redirect URI `https://TUO_DOMINIO/login/oauth2/code/google` (e
  `http://localhost:8081/login/oauth2/code/google` per i test locali). Il `.env` è in gitignore,
  quindi il secret reale risiede solo sul VPS. Il form login funziona anche se queste non sono impostate.
- **Sicurezza:** Le porte sono mappate solo su `127.0.0.1` (`127.0.0.1:8081:8081` per l'app e `127.0.0.1:5435:5432` per PostgreSQL) per impedire l'accesso esterno diretto tramite il firewall.
- **Spring Boot Multi-stage Build:** L'applicazione viene compilata ed eseguita all'interno di Docker utilizzando il `Dockerfile` multi-stage.

### 2. Caddy Reverse Proxy (Integrazione di Rete Docker)
Poiché Caddy viene eseguito in Docker (container `caddy-caddy-1`) sul VPS, sia Caddy che il container dell'applicazione (`siw_football_app`) si uniscono alla rete docker esterna condivisa `proxy-network`.
Aggiungi il gateway seguente al tuo Caddyfile:
```caddy
uni.pablo-server.178.105.2.24.sslip.io {
    reverse_proxy siw_football_app:8081
}
```

### 3. Integrazione PWA e macOS "Aggiungi al Dock"
Abbiamo aggiunto il supporto per le Progressive Web Apps (PWA) e le web app indipendenti su macOS:
- Le icone (favicon, icone touch Apple) e `site.webmanifest` si trovano in `src/main/resources/static/`.
- Il file manifest è configurato con il nome dell'app `SIW Football Manager` e il colore brand marrone (`theme_color #644a40`, coerente con `<meta name="theme-color">` su ogni pagina).
- **Configurazione di Sicurezza:** Spring Security (`SecurityConfig.java`) consente l'accesso pubblico a `/favicon.ico`, `/*.png`, `/site.webmanifest` e `/about.txt` in modo che vengano caricati da browser/macOS senza reindirizzamenti alla pagina di login.
- In Safari su macOS, seleziona **File > Aggiungi al Dock...** per installare l'applicazione con l'icona del pallone da calcio ad alta risoluzione.

### 4. Protezione Antispam alla Registrazione (Sfida Matematica + fail2ban)
Per evitare che bot dannosi intasino il database con richieste di registrazione:
- **Verifica Matematica:** Aggiunta una sfida matematica (*"Verificación humana: ¿Cuánto es 5 + 3?"*) al modulo `/register`. Il backend verifica che la risposta sia esattamente `8` prima di eseguire interrogazioni al database o codificare la password.
- **Protezione fail2ban:** Aggiunto un jail `caddy-register` sull'host per monitorare i log di Caddy e bloccare gli IP che effettuano troppe richieste POST a `/register` (più di 5 tentativi al minuto).

### 5. CI/CD con GitHub Actions
L'invio di modifiche al branch `vps-deploy` attiva il workflow `.github/workflows/deploy.yml` che effettua l'accesso al VPS tramite SSH, scarica le modifiche ed esegue:
```bash
docker compose down
docker compose up -d --build
```

### 6. Gestione Sicura del Database (TablePlus / SSH Tunnel)
Poiché PostgreSQL è mappato strettamente su `127.0.0.1:5435` sul VPS, puoi connetterti visivamente e in sicurezza dal tuo Mac usando **TablePlus** tramite un **túnel SSH** (SSH Tunnel):
- **Dettagli Connessione (PostgreSQL):**
  - **Host:** `127.0.0.1` (si riferisce al localhost del VPS stesso una volta attivo il tunnel)
  - **Port:** `5435`
  - **User / Password / Database:** Come definiti nel tuo file di configurazione `.env`.
- **Configurazione SSH Tunnel:**
  - Fai clic sul pulsante **Over SSH** in TablePlus.
  - **SSH Host:** `178.105.2.24` (IP del VPS)
  - **Port:** `22`
  - **SSH User:** `pablo`
  - **Use SSH Key:** Seleziona la tua chiave privata SSH dal tuo Mac (ad es. `~/.ssh/id_rsa` o `~/.ssh/id_ed25519`).
  - Fai clic su **Test** e poi su **Connect**.
