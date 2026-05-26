# N+1 Query Problem Analysis — SIW Football

**Author:** Pablo Rejón Camacho — Student ID 652799  
**Course:** Sistemi Informativi su Web — Università degli Studi Roma Tre  

---

## 1. Problem Description

### Scenario: Loading the Match Detail Page

**Issue detected:** Inefficient relationship loading (unoptimised fetching).

When a user visits a match detail page, Spring Data JPA loads the `Match` entity. However, to render the full view, the Thymeleaf template engine needs to access the following related entities:

- Home Team (`homeTeam`)
- Away Team (`awayTeam`)
- Tournament (`tournament`)
- Referee (`referee`)

By default, Hibernate executes **one main SQL query** for the match and then **separate queries** for each associated entity. This generates an excess of database requests known as the **N+1 Problem**, which degrades performance as data volume increases.

---

## 2. Measurement Before Optimisation (Without JOIN FETCH)

### Spring Boot Session Metrics Log (real capture)

```
Hibernate: select m1_0.id, m1_0.match_date, m1_0.home_score, m1_0.away_score,
           m1_0.status, m1_0.location, m1_0.tournament_id, m1_0.home_team_id,
           m1_0.away_team_id, m1_0.referee_id
           from match m1_0 where m1_0.id=?

Hibernate: select t1_0.id, t1_0.name, t1_0.city, t1_0.foundation_year
           from team t1_0 where t1_0.id=?

Hibernate: select t1_0.id, t1_0.name, t1_0.city, t1_0.foundation_year
           from team t1_0 where t1_0.id=?

Hibernate: select t1_0.id, t1_0.name, t1_0.description, t1_0.start_date
           from tournament t1_0 where t1_0.id=?

Hibernate: select r1_0.id, r1_0.name, r1_0.surname, r1_0.referee_code
           from referee r1_0 where r1_0.id=?

Hibernate: select c1_0.match_id, c1_0.id, c1_0.content, c1_0.user_id
           from comments c1_0 where c1_0.match_id=?

Hibernate: select u1_0.id, u1_0.username, u1_0.password, u1_0.role
           from users u1_0 where u1_0.id=?
```

### Analysis

To render **a single view**, Hibernate executed **7 separate JDBC statements**. This implies multiple round-trips between the application and PostgreSQL, increasing the total page latency to approximately **7.5 milliseconds** in query execution alone.

---

## 3. The Solution: Optimisation with JOIN FETCH

To resolve this bottleneck, the persistence layer in `MatchRepository` was modified. A custom query was implemented using the `@Query` annotation with **LEFT JOIN FETCH**.

This technique forces Hibernate to generate **a single complex SQL statement** that retrieves the match together with its relationships (`homeTeam`, `awayTeam`, `tournament` and `referee`) eagerly in one step, loading all required information into the session immediately.

### Implementation in `MatchRepository`

```java
@Query("SELECT m FROM Match m " +
       "LEFT JOIN FETCH m.homeTeam " +
       "LEFT JOIN FETCH m.awayTeam " +
       "LEFT JOIN FETCH m.tournament " +
       "LEFT JOIN FETCH m.referee " +
       "WHERE m.id = :id")
Optional<Match> findByIdWithDetails(@Param("id") Long id);
```

**Why it works:** `JOIN FETCH` is a JPQL extension that instructs Hibernate to initialise lazy associations within the same query, generating a `SELECT` with `JOIN` instead of separate queries for each relationship. Without it, Hibernate uses lazy loading and fires an additional query every time the template accesses a property of a related entity.

---

## 4. Measurement After Optimisation (With JOIN FETCH)

### Spring Boot Session Metrics Log (real capture)

```
Hibernate: select m1_0.id, m1_0.match_date, m1_0.home_score, m1_0.away_score,
           m1_0.status, m1_0.location,
           t1_0.id, t1_0.name, t1_0.description,
           h1_0.id, h1_0.name, h1_0.city,
           a1_0.id, a1_0.name, a1_0.city,
           r1_0.id, r1_0.name, r1_0.surname, r1_0.referee_code
           from match m1_0
           left join tournament t1_0 on t1_0.id = m1_0.tournament_id
           left join team h1_0 on h1_0.id = m1_0.home_team_id
           left join team a1_0 on a1_0.id = m1_0.away_team_id
           left join referee r1_0 on r1_0.id = m1_0.referee_id
           where m1_0.id=?

Hibernate: select c1_0.match_id, c1_0.id, c1_0.content, c1_0.user_id
           from comments c1_0 where c1_0.match_id=?

Hibernate: select u1_0.id, u1_0.username, u1_0.password, u1_0.role
           from users u1_0 where u1_0.id=?
```

### Performance Comparison Table

| Metric | Without optimisation | With `JOIN FETCH` | Improvement |
|--------|:--------------------:|:-----------------:|:-----------:|
| JDBC queries executed | **7** | **3** | −57% |
| Total execution time | ~7.5 ms | ~3.6 ms | **52% faster** |
| Round-trips to PostgreSQL | 7 | 3 | −57% |
| Scalability under load | Linear N·relations | Constant | High |

### Final Analysis

After optimisation, the number of queries dropped from 7 to just **3 JDBC statements** (a 57% reduction). The main query groups the entire match structure into a single `SELECT` with `JOIN`. Execution time fell drastically to **3.6 milliseconds**, making the application twice as fast and significantly more scalable under high user load.

---

*Pablo Rejón Camacho — Student ID 652799*  
*Sistemi Informativi su Web — Università degli Studi Roma Tre*
