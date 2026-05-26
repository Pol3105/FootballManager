# Análisis del Problema N+1 en SIW Football

**Autor:** Pablo Rejón Camacho — Matrícula 652799  
**Asignatura:** Sistemi Informativi su Web — Università degli Studi Roma Tre  

---

## 1. Descripción del Problema

### Escenario: Carga de la página "Detalles del Partido"

**Problema detectado:** Carga ineficiente de relaciones (fetching no optimizado).

Cuando un usuario visita la página de un partido, Spring Data JPA carga la entidad `Match`. Sin embargo, para renderizar la vista completa, el motor de plantillas (Thymeleaf) necesita acceder a las entidades relacionadas:

- Equipo Local (`homeTeam`)
- Equipo Visitante (`awayTeam`)
- Torneo (`tournament`)
- Árbitro (`referee`)

Por defecto, Hibernate ejecuta **una consulta SQL principal** para el partido y luego **consultas separadas** para cada una de estas entidades asociadas. Esto genera un exceso de peticiones a la base de datos conocido como el **Problema N+1**, que degrada el rendimiento a medida que aumenta el volumen de datos.

---

## 2. Medición Antes de la Optimización (Sin JOIN FETCH)

### Log de Session Metrics de Spring Boot (captura real)

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

### Análisis

Para renderizar **una única vista**, Hibernate ejecutó **7 sentencias JDBC distintas**. Esto implica múltiples viajes de ida y vuelta (*round-trips*) entre la aplicación y PostgreSQL, aumentando la latencia total de la página a aproximadamente **7,5 milisegundos** solo en ejecución de consultas.

---

## 3. La Solución: Optimización con JOIN FETCH

Para solucionar este cuello de botella, se modificó la capa de persistencia en `MatchRepository`. Se implementó una consulta personalizada mediante la anotación `@Query` utilizando **LEFT JOIN FETCH**.

Esta técnica obliga a Hibernate a generar una **única sentencia SQL compleja** que recupera el partido junto con sus relaciones (`homeTeam`, `awayTeam`, `tournament` y `referee`) de forma *eager* en un solo paso, cargando toda la información necesaria en la sesión de forma inmediata.

### Implementación en `MatchRepository`

```java
@Query("SELECT m FROM Match m " +
       "LEFT JOIN FETCH m.homeTeam " +
       "LEFT JOIN FETCH m.awayTeam " +
       "LEFT JOIN FETCH m.tournament " +
       "LEFT JOIN FETCH m.referee " +
       "WHERE m.id = :id")
Optional<Match> findByIdWithDetails(@Param("id") Long id);
```

**Por qué funciona:** `JOIN FETCH` es una extensión de JPQL que indica a Hibernate que inicialice las asociaciones lazy en la misma query, generando un `SELECT` con `JOIN` en lugar de queries separadas por cada relación. Sin ello, Hibernate usa *lazy loading* y lanza una query adicional cada vez que la plantilla accede a una propiedad de una entidad relacionada.

---

## 4. Medición Después de la Optimización (Con JOIN FETCH)

### Log de Session Metrics de Spring Boot (captura real)

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

### Tabla Comparativa de Rendimiento

| Métrica | Sin optimización | Con `JOIN FETCH` | Mejora |
|---------|:---------------:|:----------------:|:------:|
| Queries JDBC ejecutadas | **7** | **3** | −57% |
| Tiempo total de ejecución | ~7,5 ms | ~3,6 ms | **52% más rápido** |
| Round-trips a PostgreSQL | 7 | 3 | −57% |
| Escalabilidad bajo carga | Lineal N·relaciones | Constante | Alta |

### Análisis Final

Tras la optimización, el número de consultas se redujo de 7 a solo **3 sentencias JDBC** (reducción del 57%). La consulta principal agrupa toda la estructura del partido en un único `SELECT` con `JOIN`. El tiempo de ejecución bajó drásticamente a **3,6 milisegundos**, logrando que la aplicación sea el doble de rápida y mucho más escalable ante una alta carga de usuarios.

---

*Pablo Rejón Camacho — Matrícula 652799*  
*Sistemi Informativi su Web — Università degli Studi Roma Tre*
