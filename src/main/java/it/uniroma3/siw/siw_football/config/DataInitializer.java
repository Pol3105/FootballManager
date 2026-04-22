package it.uniroma3.siw.siw_football.config;

import java.time.LocalDate;
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
    @Autowired private TeamService teamService;
    @Autowired private PlayerService playerService;
    @Autowired private PlayerRepository playerRepository;
    @Autowired private MatchService matchService;
    @Autowired private MatchRepository matchRepository;
    @Autowired private RefereeService refereeService;
    @Autowired private RefereeRepository refereeRepository;

    @Override
    public void run(String... args) throws Exception {
        
        // --- FASE 1: MÚLTIPLES TORNEOS Y EQUIPOS (ManyToMany) ---
        if (tournamentRepository.count() == 0) {
            System.out.println("⚽ INICIANDO INYECCIÓN MASIVA...");

            // 1. Torneos
            Tournament champions = createTournament("Champions League 2026", LocalDate.of(2026, 9, 1));
            Tournament mundial = createTournament("Mundial 2026", LocalDate.of(2026, 6, 10));
            Tournament euro = createTournament("Eurocopa 2024", LocalDate.of(2024, 6, 14));

            // 2. Equipos (Clubes y Selecciones)
            Team madrid = createTeam("Real Madrid", 1902);
            Team roma = createTeam("AS Roma", 1927);
            Team city = createTeam("Manchester City", 1880);
            Team barca = createTeam("FC Barcelona", 1899);
            Team spain = createTeam("España", 1920);
            Team argentina = createTeam("Argentina", 1893);
            Team italy = createTeam("Italia", 1898);

            // 3. Asignaciones ManyToMany
            // Equipos en Champions
            tournamentService.addTeamToTournament(champions.getId(), madrid.getId());
            tournamentService.addTeamToTournament(champions.getId(), roma.getId());
            tournamentService.addTeamToTournament(champions.getId(), city.getId());
            tournamentService.addTeamToTournament(champions.getId(), barca.getId());

            // Equipos en Mundial
            tournamentService.addTeamToTournament(mundial.getId(), spain.getId());
            tournamentService.addTeamToTournament(mundial.getId(), argentina.getId());
            tournamentService.addTeamToTournament(mundial.getId(), italy.getId());

            // Equipos en Euro
            tournamentService.addTeamToTournament(euro.getId(), spain.getId());
            tournamentService.addTeamToTournament(euro.getId(), italy.getId());

            System.out.println("✅ TORNEOS Y EQUIPOS CONFIGURADOS.");
        }

        // --- FASE 2: PLANTILLAS MÁS COMPLETAS ---
        if (playerRepository.count() == 0) {
            System.out.println("🏃 RECLUTANDO JUGADORES...");

            Team madrid = teamRepository.findByName("Real Madrid").get(0);
            Team roma = teamRepository.findByName("AS Roma").get(0);
            Team city = teamRepository.findByName("Manchester City").get(0);
            Team argentina = teamRepository.findByName("Argentina").get(0);

            // Real Madrid
            createAndAssignPlayer("Vinícius", "Júnior", "Forward", madrid);
            createAndAssignPlayer("Jude", "Bellingham", "Midfielder", madrid);
            createAndAssignPlayer("Kylian", "Mbappé", "Forward", madrid);
            createAndAssignPlayer("Luka", "Modric", "Midfielder", madrid);

            // AS Roma
            createAndAssignPlayer("Paulo", "Dybala", "Forward", roma);
            createAndAssignPlayer("Lorenzo", "Pellegrini", "Midfielder", roma);
            createAndAssignPlayer("Stephan", "El Shaarawy", "Forward", roma);

            // Man City
            createAndAssignPlayer("Erling", "Haaland", "Forward", city);
            createAndAssignPlayer("Kevin", "De Bruyne", "Midfielder", city);
            createAndAssignPlayer("Rodri", "Hernández", "Midfielder", city);

            // Argentina
            createAndAssignPlayer("Lionel", "Messi", "Forward", argentina);
            createAndAssignPlayer("Enzo", "Fernández", "Midfielder", argentina);

            System.out.println("✅ PLANTILLAS COMPLETADAS.");
        }

        // --- FASE 3: ÁRBITROS Y PARTIDOS POR TORNEO ---
        if (matchRepository.count() == 0) {
            System.out.println("🏟️ PROGRAMANDO CALENDARIO...");

            Referee collina = createReferee("Pierluigi", "Collina");
            Referee marciniak = createReferee("Szymon", "Marciniak");
            Referee webb = createReferee("Howard", "Webb");

            Tournament champions = tournamentRepository.findByName("Champions League 2026").get(0);
            Tournament mundial = tournamentRepository.findByName("Mundial 2026").get(0);

            Team madrid = teamRepository.findByName("Real Madrid").get(0);
            Team city = teamRepository.findByName("Manchester City").get(0);
            Team roma = teamRepository.findByName("AS Roma").get(0);
            Team barca = teamRepository.findByName("FC Barcelona").get(0);
            Team argentina = teamRepository.findByName("Argentina").get(0);
            Team spain = teamRepository.findByName("España").get(0);

            // Partidos de Champions
            createMatch(madrid, city, champions, collina, 3, 3, LocalDate.of(2026, 4, 15));
            createMatch(roma, barca, champions, marciniak, 1, 0, LocalDate.of(2026, 4, 16));

            // Partido de Mundial
            createMatch(argentina, spain, mundial, webb, 2, 1, LocalDate.of(2026, 7, 10));

            System.out.println("✅ CALENDARIO GENERADO.");
        }
    }

    // --- MÉTODOS AUXILIARES PARA LIMPIAR EL CÓDIGO ---

    private Tournament createTournament(String name, LocalDate date) {
        Tournament t = new Tournament();
        t.setName(name);
        t.setStartDate(date);
        return tournamentService.saveTournament(t);
    }

    private Team createTeam(String name, int year) {
        Team t = new Team();
        t.setName(name);
        t.setFoundationYear(year);
        return teamRepository.save(t);
    }

    private void createAndAssignPlayer(String name, String surname, String pos, Team team) {
        Player p = new Player();
        p.setName(name);
        p.setSurname(surname);
        p.setPosition(pos);
        p = playerService.savePlayer(p);
        playerService.assignPlayerToTeam(p.getId(), team.getId());
    }

    private Referee createReferee(String name, String surname) {
        Referee r = new Referee();
        r.setName(name);
        r.setSurname(surname);
        return refereeService.saveReferee(r);
    }

    private void createMatch(Team home, Team away, Tournament t, Referee r, int hScore, int aScore, LocalDate date) {
        Match m = new Match();
        m.setHomeTeam(home);
        m.setAwayTeam(away);
        m.setTournament(t);
        m.setReferee(r);
        m.setHomeScore(hScore);
        m.setAwayScore(aScore);
        m.setMatchDate(date.atTime(21, 0));
        matchService.saveMatch(m);
    }
}