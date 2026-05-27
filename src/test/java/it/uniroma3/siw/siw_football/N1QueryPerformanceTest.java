package it.uniroma3.siw.siw_football;

import it.uniroma3.siw.siw_football.model.Match;
import it.uniroma3.siw.siw_football.repository.MatchRepository;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Análisis experimental del Problema N+1 — SIW Football
 *
 * Compara el número de queries JDBC generadas por dos estrategias:
 *   1. findById()          — lazy loading, múltiples queries (problema N+1)
 *   2. findByIdWithDetails() — JOIN FETCH, una sola query (solución)
 *
 * Requiere la base de datos PostgreSQL activa con datos de demo cargados.
 * Arrancar con: docker-compose up -d  &&  ./mvnw spring-boot:run (una vez para seed)
 * Luego ejecutar: ./mvnw test -Dtest=N1QueryPerformanceTest
 */
@SpringBootTest
class N1QueryPerformanceTest {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Statistics hibernateStats;
    private Long testMatchId;

    @BeforeEach
    void setUp() {
        hibernateStats = entityManagerFactory
                .unwrap(SessionFactory.class)
                .getStatistics();
        hibernateStats.setStatisticsEnabled(true);

        // Usar el primer partido disponible (cargado por DataInitializer)
        List<Match> matches = matchRepository.findAll();
        assertFalse(matches.isEmpty(), "La BD debe tener partidos. Arrancar la app una vez para que DataInitializer inserte datos.");
        testMatchId = matches.get(0).getId();
    }

    // -------------------------------------------------------------------------
    // TEST 1: Problema N+1 — sin optimización
    // -------------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("ANTES: findById genera múltiples queries JDBC (problema N+1)")
    void naiveFindById_generatesMultipleQueries() {

        hibernateStats.clear();

        // 1 query: SELECT match
        Match match = matchRepository.findById(testMatchId).orElseThrow();

        // Cada acceso a relación LAZY dispara una query adicional:
        String homeTeamName  = match.getHomeTeam().getName();    // +1 query
        String awayTeamName  = match.getAwayTeam().getName();    // +1 query
        String tournamentName = match.getTournament().getName(); // +1 query
        String refereeName   = match.getReferee().getName();     // +1 query
        int commentCount     = match.getComments().size();       // +1 query (colección)

        // Si hay comentarios, acceder al usuario de cada uno: +1 query por usuario único
        match.getComments().forEach(c -> {
            String username = c.getUser().getUsername();         // +1 query por usuario
        });

        long queryCount = hibernateStats.getPrepareStatementCount();

        System.out.println("\n========================================");
        System.out.println("  RESULTADO — Sin optimización (N+1)");
        System.out.println("========================================");
        System.out.printf("  Partido ID      : %d%n", testMatchId);
        System.out.printf("  Home team       : %s%n", homeTeamName);
        System.out.printf("  Away team       : %s%n", awayTeamName);
        System.out.printf("  Torneo          : %s%n", tournamentName);
        System.out.printf("  Árbitro         : %s%n", refereeName);
        System.out.printf("  Comentarios     : %d%n", commentCount);
        System.out.printf("  Queries JDBC    : %d%n", queryCount);
        System.out.println("  Esperado        : >= 6 queries");
        System.out.println("========================================\n");

        // Sin optimización: 1 (match) + 4 (relaciones) + 1 (comentarios) + N (usuarios) = >= 6
        assertTrue(queryCount >= 6,
            "Sin JOIN FETCH deben ejecutarse >= 6 queries. Actual: " + queryCount);
    }

    // -------------------------------------------------------------------------
    // TEST 2: Solución JOIN FETCH — una sola query principal
    // -------------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("DESPUÉS: findByIdWithDetails genera solo 3 queries JDBC (JOIN FETCH)")
    void joinFetchFindByIdWithDetails_generatesFewerQueries() {

        hibernateStats.clear();

        // 1 query: SELECT match JOIN homeTeam JOIN awayTeam JOIN tournament JOIN referee
        Match match = matchRepository.findByIdWithDetails(testMatchId).orElseThrow();

        // Estas relaciones ya están cargadas — NO disparan queries adicionales:
        String homeTeamName   = match.getHomeTeam().getName();    // 0 queries extra
        String awayTeamName   = match.getAwayTeam().getName();    // 0 queries extra
        String tournamentName = match.getTournament().getName();  // 0 queries extra
        String refereeName    = match.getReferee().getName();     // 0 queries extra

        // Comentarios siguen siendo lazy (no incluidos en este JOIN FETCH):
        int commentCount = match.getComments().size();            // +1 query
        match.getComments().forEach(c -> {
            String username = c.getUser().getUsername();          // +1 query por usuario único
        });

        long queryCount = hibernateStats.getPrepareStatementCount();

        System.out.println("\n========================================");
        System.out.println("  RESULTADO — Con JOIN FETCH (optimizado)");
        System.out.println("========================================");
        System.out.printf("  Partido ID      : %d%n", testMatchId);
        System.out.printf("  Home team       : %s%n", homeTeamName);
        System.out.printf("  Away team       : %s%n", awayTeamName);
        System.out.printf("  Torneo          : %s%n", tournamentName);
        System.out.printf("  Árbitro         : %s%n", refereeName);
        System.out.printf("  Comentarios     : %d%n", commentCount);
        System.out.printf("  Queries JDBC    : %d%n", queryCount);
        System.out.printf("  Esperado        : 1 + 1 (comments) + N usuarios%n");
        System.out.println("========================================\n");

        // Con JOIN FETCH: 1 (match+homeTeam+awayTeam+tournament+referee en una SQL)
        //                + 1 (comments, lazy)
        //                + N (1 por cada usuario único en comentarios)
        // El número exacto depende de los comentarios del partido.
        // Lo importante: SIGNIFICATIVAMENTE menos que las 7+ del caso naive.
        assertTrue(queryCount < 6,
            "Con JOIN FETCH deben ejecutarse < 6 queries (vs 7+ naive). Actual: " + queryCount);
    }

    // -------------------------------------------------------------------------
    // TEST 3: Comparación directa de mejora
    // -------------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("COMPARACIÓN: JOIN FETCH reduce queries significativamente respecto a findById")
    void joinFetch_isSignificantlyMoreEfficientThanNaive() {

        // --- Medir naive ---
        hibernateStats.clear();
        Match naive = matchRepository.findById(testMatchId).orElseThrow();
        naive.getHomeTeam().getName();
        naive.getAwayTeam().getName();
        naive.getTournament().getName();
        naive.getReferee().getName();
        naive.getComments().size();
        naive.getComments().forEach(c -> c.getUser().getUsername());
        long naiveCount = hibernateStats.getPrepareStatementCount();

        // --- Medir optimizado ---
        hibernateStats.clear();
        Match optimized = matchRepository.findByIdWithDetails(testMatchId).orElseThrow();
        optimized.getHomeTeam().getName();
        optimized.getAwayTeam().getName();
        optimized.getTournament().getName();
        optimized.getReferee().getName();
        optimized.getComments().size();
        optimized.getComments().forEach(c -> c.getUser().getUsername());
        long optimizedCount = hibernateStats.getPrepareStatementCount();

        double reduction = 100.0 * (naiveCount - optimizedCount) / naiveCount;

        System.out.println("\n========================================");
        System.out.println("  COMPARACIÓN FINAL N+1 vs JOIN FETCH");
        System.out.println("========================================");
        System.out.printf("  Sin optimización  : %d queries%n", naiveCount);
        System.out.printf("  Con JOIN FETCH    : %d queries%n", optimizedCount);
        System.out.printf("  Reducción         : %.0f%%%n", reduction);
        System.out.println("========================================\n");

        assertTrue(optimizedCount < naiveCount,
            "JOIN FETCH debe generar menos queries que findById naive");
        assertTrue(reduction >= 40,
            String.format("Mejora esperada >= 40%%. Actual: %.0f%%", reduction));
    }
}
