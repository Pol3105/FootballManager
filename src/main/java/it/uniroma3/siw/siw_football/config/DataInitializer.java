package it.uniroma3.siw.siw_football.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import it.uniroma3.siw.siw_football.model.*;
import it.uniroma3.siw.siw_football.repository.*;
import it.uniroma3.siw.siw_football.service.*;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private TournamentService tournamentService;
    @Autowired private TournamentRepository tournamentRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private PlayerService playerService;
    @Autowired private PlayerRepository playerRepository;
    @Autowired private MatchService matchService;
    @Autowired private MatchRepository matchRepository;
    @Autowired private RefereeService refereeService;
    @Autowired private RefereeRepository refereeRepository;

    @Override
    public void run(String... args) throws Exception {
        
        // --- FASE 1: TORNEOS Y EQUIPOS (Con Descripción y Ciudad) ---
        if (tournamentRepository.count() == 0) {
            System.out.println("⚽ INICIANDO INYECCIÓN DE DATOS (REQUISITOS PDF)...");

            // 1. Torneos [cite: 22-26]
            Tournament champions = createTournament("Champions League 2026", LocalDate.of(2026, 9, 1), "La mayor competición de clubes de Europa.");
            Tournament mundial = createTournament("Mundial 2026", LocalDate.of(2026, 6, 10), "Copa del Mundo de la FIFA en Norteamérica.");
            Tournament euro = createTournament("Eurocopa 2024", LocalDate.of(2024, 6, 14), "Competición de selecciones europeas.");

            // 2. Equipos (Con Ciudad) [cite: 27-30]
            Team madrid = createTeam("Real Madrid", 1902, "Madrid");
            Team roma = createTeam("AS Roma", 1927, "Roma");
            Team city = createTeam("Manchester City", 1880, "Manchester");
            Team barca = createTeam("FC Barcelona", 1899, "Barcelona");
            Team spain = createTeam("España", 1920, "Madrid");
            Team argentina = createTeam("Argentina", 1893, "Buenos Aires");
            Team italy = createTeam("Italia", 1898, "Roma");

            // 3. Asignaciones ManyToMany [cite: 32]
            tournamentService.addTeamToTournament(champions.getId(), madrid.getId());
            tournamentService.addTeamToTournament(champions.getId(), roma.getId());
            tournamentService.addTeamToTournament(champions.getId(), city.getId());
            tournamentService.addTeamToTournament(champions.getId(), barca.getId());

            tournamentService.addTeamToTournament(mundial.getId(), spain.getId());
            tournamentService.addTeamToTournament(mundial.getId(), argentina.getId());
            tournamentService.addTeamToTournament(mundial.getId(), italy.getId());

            System.out.println("✅ TORNEOS Y EQUIPOS CONFIGURADOS.");
        }

        // --- FASE 2: JUGADORES (Con Fecha Nac. y Altura) [cite: 34-39] ---
        if (playerRepository.count() == 0) {
            System.out.println("🏃 RECLUTANDO JUGADORES...");

            Team madrid = teamRepository.findByName("Real Madrid").get(0);
            Team roma = teamRepository.findByName("AS Roma").get(0);

            // Real Madrid
            createAndAssignPlayer("Vinícius", "Júnior", "Forward", LocalDate.of(2000, 7, 12), 1.76, madrid);
            createAndAssignPlayer("Jude", "Bellingham", "Midfielder", LocalDate.of(2003, 6, 29), 1.86, madrid);
            createAndAssignPlayer("Kylian", "Mbappé", "Forward", LocalDate.of(1998, 12, 20), 1.78, madrid);

            // AS Roma
            createAndAssignPlayer("Paulo", "Dybala", "Forward", LocalDate.of(1993, 11, 15), 1.77, roma);
            createAndAssignPlayer("Lorenzo", "Pellegrini", "Midfielder", LocalDate.of(1996, 6, 19), 1.86, roma);

            System.out.println("✅ PLANTILLAS COMPLETADAS.");
        }

        // --- FASE 3: ÁRBITROS Y PARTIDOS (Con Código, Estado y Lugar) [cite: 42-55] ---
        if (matchRepository.count() == 0) {
            System.out.println("🏟️ PROGRAMANDO CALENDARIO...");

            // Árbitros con Código Arbitral 
            Referee collina = createReferee("Pierluigi", "Collina", "REF-ITA-001");
            Referee marciniak = createReferee("Szymon", "Marciniak", "REF-POL-002");

            Tournament champions = tournamentRepository.findByName("Champions League 2026").get(0);
            Team madrid = teamRepository.findByName("Real Madrid").get(0);
            Team city = teamRepository.findByName("Manchester City").get(0);

            // Partidos con Estado y Lugar 
            createMatch(madrid, city, champions, collina, 3, 3, LocalDateTime.of(2026, 4, 15, 21, 0), MatchStatus.PLAYED, "Santiago Bernabéu");
            createMatch(city, madrid, champions, marciniak, 0, 0, LocalDateTime.of(2026, 4, 22, 21, 0), MatchStatus.SCHEDULED, "Etihad Stadium");

            System.out.println("✅ CALENDARIO GENERADO.");
        }
    }

    // --- MÉTODOS AUXILIARES ACTUALIZADOS ---

    private Tournament createTournament(String name, LocalDate date, String desc) {
        Tournament t = new Tournament();
        t.setName(name);
        t.setStartDate(date);
        t.setDescription(desc); // 
        return tournamentService.saveTournament(t);
    }

    private Team createTeam(String name, int year, String city) {
        Team t = new Team();
        t.setName(name);
        t.setFoundationYear(year);
        t.setCity(city); // 
        return teamRepository.save(t);
    }

    private void createAndAssignPlayer(String name, String surname, String pos, LocalDate birth, Double height, Team team) {
        Player p = new Player();
        p.setName(name);
        p.setSurname(surname);
        p.setPosition(pos);
        p.setBirthDate(birth); // [cite: 37]
        p.setHeight(height);   // [cite: 39]
        p = playerService.savePlayer(p);
        playerService.assignPlayerToTeam(p.getId(), team.getId());
    }

    private Referee createReferee(String name, String surname, String code) {
        Referee r = new Referee();
        r.setName(name);
        r.setSurname(surname);
        r.setRefereeCode(code); // 
        return refereeService.saveReferee(r);
    }

    private void createMatch(Team home, Team away, Tournament t, Referee r, int hScore, int aScore, LocalDateTime date, MatchStatus status, String loc) {
        Match m = new Match();
        m.setHomeTeam(home);
        m.setAwayTeam(away);
        m.setTournament(t);
        m.setReferee(r);
        m.setHomeScore(hScore);
        m.setAwayScore(aScore);
        m.setMatchDate(date);
        m.setStatus(status);   // [cite: 47]
        m.setLocation(loc);     // [cite: 44]
        matchService.saveMatch(m);
    }
}