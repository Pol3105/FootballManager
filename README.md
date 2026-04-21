# ⚽ SIW Project: Football Tournament Management

Project for the *Sistemi Informativi su Web (SIW)* course. 
A comprehensive web application to manage football tournaments, teams, players, matches, and referees, strictly following the 3-Layer Architecture pattern.

## 🛠️ Tech Stack
* **Backend:** Java 21, Spring Boot 3
* **Persistence:** Spring Data JPA, Hibernate
* **Database:** PostgreSQL 15 (Dockerized)
* **Frontend:** Thymeleaf, HTML5, CSS3 *(Coming soon)*
* **Security:** Spring Security *(Coming soon)*

## 🏛️ Architecture Overview
This project is built using a strict 3-Layer Architecture:
1. **Persistence Layer (`model` & `repository`):** Domain entities mapped to PostgreSQL via Hibernate ORM. Leverages `CrudRepository` for database operations.
2. **Business/Service Layer (`service`):** Contains business logic, coordinates repositories, and ensures data integrity using `@Transactional`.
3. **Presentation Layer (`controller`):** Handles HTTP requests and MVC flow.

### Current Domain Model
* `User`: System users with role-based access (ADMIN/USER).
* `Tournament`: The root entity representing a football competition. Contains multiple matches and teams.
* `Team`: Represents a football club. Linked to a tournament and contains multiple players.
* `Player`: Represents a footballer, linked to a specific `Team`.
* `Referee`: Officiates matches.
* `Match`: Complex entity linking two `Team` entities (Home/Away using `@JoinColumn`), one `Referee`, and one `Tournament`.

## 🚀 How to run locally

### 1. Start the Database Environment
Ensure Docker Desktop is running, then execute:

docker-compose up -d;

The DataInitializer component will automatically seed the database with test data (e.g., Champions League, Real Madrid, AS Roma) on the first run


## 📅 Changelog

### Phase 1

Initial Spring Boot setup. Configured PostgreSQL via Docker Compose. Established GitHub repository.

Implemented the core Domain Model (User, Tournament, Team). Configured JPA @OneToMany/@ManyToOne relationships. Created Repositories and TournamentService. Implemented DataInitializer for automatic database seeding.

### Phase 2: Complete Domain Model & Complex Relationships

* **Entities & Persistence:** Implemented the remaining mandatory entities (`Player`, `Referee`, `Match`).
* **Complex Mapping:** Configured advanced JPA relationships, specifically managing multiple foreign keys from `Match` to `Team` (`home_team_id` and `away_team_id`) using `@JoinColumn`, and linking matches to their respective `Tournament`.
* **Business Logic:** Created `PlayerService`, `RefereeService`, and `MatchService` to enforce the 3-Layer architecture rule (no direct repository access from controllers/initializers).
* **Data Seeding:** Upgraded the `DataInitializer` with a phased execution to safely inject players (e.g., Vinícius, Dybala), referees (Collina), and a complete match (Real Madrid vs AS Roma) without duplicating data on restarts.

### Phase 3: Presentation Layer, Spring Security & Admin CRUD

* **Navigation Flow:** Implemented a full user journey from the Index (Tournament list) to Tournament Details (Match calendar and Participating teams) and finally to Team Details (Full player rosters).

* **Strict 3-Layer Enforcement:** Refactored the TournamentController to exclusively interact with the TournamentService, ensuring the Repository remains encapsulated.

* **Tournament Management (CRUD):**
    -Implemented full lifecycle management for Tournament entities.

    -Developed a dynamic Form Handling system with Thymeleaf, capable of distinguishing between Create and Update operations via ID persistence.

    -Integrated @DateTimeFormat to synchronize ISO date inputs from HTML5 with Java LocalDate objects.

* **UI/UX Enhancements:**
    -Modular CSS: Transitioned from inline styles to a structured CSS architecture (global.css, tournament.css, team.css) served from the Spring Boot static resources.

    -Interactivity: Integrated SweetAlert2 for administrative delete confirmations, replacing standard browser alerts with a modern UI.



