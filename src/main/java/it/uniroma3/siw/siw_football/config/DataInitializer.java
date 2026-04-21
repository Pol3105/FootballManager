package it.uniroma3.siw.siw_football.config;


import it.uniroma3.siw.siw_football.model.Match;
import it.uniroma3.siw.siw_football.model.Player;
import it.uniroma3.siw.siw_football.model.Referee;
import it.uniroma3.siw.siw_football.model.Team;
import it.uniroma3.siw.siw_football.model.Tournament;
import it.uniroma3.siw.siw_football.repository.MatchRepository;
import it.uniroma3.siw.siw_football.repository.PlayerRepository;
import it.uniroma3.siw.siw_football.repository.RefereeRepository;
import it.uniroma3.siw.siw_football.repository.TeamRepository;
import it.uniroma3.siw.siw_football.repository.TournamentRepository;
import it.uniroma3.siw.siw_football.service.MatchService;
import it.uniroma3.siw.siw_football.service.PlayerService;
import it.uniroma3.siw.siw_football.service.RefereeService;
import it.uniroma3.siw.siw_football.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private TournamentService tournamentService;
    @Autowired private TournamentRepository tournamentRepository;
    @Autowired private TeamRepository teamRepository;
    
    // Inyectamos nuestras nuevas herramientas para los Jugadores
    @Autowired private PlayerService playerService;
    @Autowired private PlayerRepository playerRepository;

    @Autowired private MatchService matchService;
    @Autowired private MatchRepository matchRepository;
    @Autowired private RefereeService refereeService;
    @Autowired private RefereeRepository refereeRepository;

    @Override
    public void run(String... args) throws Exception {
        
        // --- FASE 1: TORNEOS Y EQUIPOS ---
        if (tournamentRepository.count() == 0) {
            System.out.println("⚽ LA BD ESTÁ VACÍA. INICIANDO INYECCIÓN DE TORNEOS...");

            Tournament champions = new Tournament();
            champions.setName("Champions League 2026");
            champions.setDate(LocalDate.of(2026, 9, 1));
            champions = tournamentService.saveTournament(champions);

            Team madrid = new Team();
            madrid.setName("Real Madrid");
            madrid.setFoundationYear(1902);
            madrid = teamRepository.save(madrid);

            Team roma = new Team();
            roma.setName("AS Roma");
            roma.setFoundationYear(1927);
            roma = teamRepository.save(roma);

            tournamentService.addTeamToTournament(champions.getId(), madrid.getId());
            tournamentService.addTeamToTournament(champions.getId(), roma.getId());

            System.out.println("✅ TORNEOS Y EQUIPOS INYECTADOS.");
        } else {
            System.out.println("👍 LOS TORNEOS YA EXISTEN. Omitiendo fase 1.");
        }

        // --- FASE 2: JUGADORES ---
        if (playerRepository.count() == 0) {
            System.out.println("🏃 INYECTANDO JUGADORES...");

            // Recuperamos los equipos de la base de datos para asignarles los jugadores
            Iterable<Team> teams = teamRepository.findAll();
            Team madrid = null;
            Team roma = null;

            for (Team t : teams) {
                if (t.getName().equals("Real Madrid")) madrid = t;
                if (t.getName().equals("AS Roma")) roma = t;
            }

            // Fichajes del Real Madrid
            if (madrid != null) {
                Player vini = new Player();
                vini.setName("Vinícius");
                vini.setSurname("Júnior");
                vini.setPosition("Forward");
                vini = playerService.savePlayer(vini);
                playerService.assignPlayerToTeam(vini.getId(), madrid.getId());

                Player jude = new Player();
                jude.setName("Jude");
                jude.setSurname("Bellingham");
                jude.setPosition("Midfielder");
                jude = playerService.savePlayer(jude);
                playerService.assignPlayerToTeam(jude.getId(), madrid.getId());
            }

            // Fichajes de la Roma
            if (roma != null) {
                Player dybala = new Player();
                dybala.setName("Paulo");
                dybala.setSurname("Dybala");
                dybala.setPosition("Forward");
                dybala = playerService.savePlayer(dybala);
                playerService.assignPlayerToTeam(dybala.getId(), roma.getId());
            }

            System.out.println("✅ JUGADORES INYECTADOS CON ÉXITO.");
        } else {
            System.out.println("👍 LOS JUGADORES YA EXISTEN. Omitiendo fase 2.");
        }

        // --- FASE 3: ÁRBITROS Y PARTIDOS ---
        if (matchRepository.count() == 0) {
            System.out.println("🏟️ PREPARANDO EL GRAN PARTIDO...");

            // 1. Creamos un árbitro legendario
            Referee collina = new Referee();
            collina.setName("Pierluigi");
            collina.setSurname("Collina");
            collina = refereeService.saveReferee(collina);

            // 2. Recuperamos los equipos (sabemos que existen por la Fase 1)
            Iterable<Team> teams = teamRepository.findAll();
            Team madrid = null;
            Team roma = null;

            for (Team t : teams) {
                if (t.getName().equals("Real Madrid")) madrid = t;
                if (t.getName().equals("AS Roma")) roma = t;
            }

            // Recuperamos los torneos de la base de datos
            Iterable<Tournament> tournaments = tournamentRepository.findAll();
            Tournament champions = null;

            for (Tournament t : tournaments) {
                if (t.getName().equals("Champions League 2026")) {
                    champions = t;
                    break;
                }
            }


            // 3. Creamos el partido
            if (madrid != null && roma != null) {
                Match finalMatch = new Match();
                finalMatch.setMatchDate(LocalDate.of(2026, 5, 30).atTime(21, 0)); // 30 de Mayo a las 21:00
                finalMatch.setHomeTeam(madrid);
                finalMatch.setAwayTeam(roma);
                finalMatch.setReferee(collina);
                
                // Pongamos un resultado ficticio
                finalMatch.setHomeScore(3);
                finalMatch.setAwayScore(1);

                
                finalMatch.setTournament(champions);
                
                matchService.saveMatch(finalMatch);
                System.out.println("✅ PARTIDO CREADO: Real Madrid 3 - 1 AS Roma (Árbitro: Collina)");
            }
        } else {
            System.out.println("👍 EL PARTIDO YA EXISTE. Omitiendo fase 3.");
        }


    }
}