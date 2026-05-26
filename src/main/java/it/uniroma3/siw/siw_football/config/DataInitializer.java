package it.uniroma3.siw.siw_football.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    @Autowired private UserService userService;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // --- FASE 0: USUARIOS ---
        createUsers();

        // --- FASE 1: TORNEOS Y EQUIPOS ---
        if (tournamentRepository.count() == 0) {
            System.out.println("⚽ INICIANDO INYECCIÓN DE DATOS...");

            // Torneos
            Tournament champions = createTournament("UEFA Champions League 2025/26",
                LocalDate.of(2025, 9, 16),
                "La competición de clubes más prestigiosa del mundo. 32 equipos de élite compiten por la gloria europea.");
            Tournament laliga = createTournament("La Liga 2025/26",
                LocalDate.of(2025, 8, 15),
                "Primera División del fútbol español. 20 equipos luchan por el título de la liga más vista del planeta.");
            Tournament serieA = createTournament("Serie A 2025/26",
                LocalDate.of(2025, 8, 24),
                "La liga italiana, cuna del calcio. Hogar de los mejores defensas y tácticas del mundo.");

            // Equipos — Clubes europeos
            Team madrid    = createTeam("Real Madrid",       1902, "Madrid");
            Team barca     = createTeam("FC Barcelona",      1899, "Barcelona");
            Team atletico  = createTeam("Atlético de Madrid",1903, "Madrid");
            Team sevilla   = createTeam("Sevilla FC",        1890, "Sevilla");
            Team city      = createTeam("Manchester City",   1880, "Manchester");
            Team liverpool = createTeam("Liverpool FC",      1892, "Liverpool");
            Team psg       = createTeam("Paris Saint-Germain",1970,"París");
            Team bayern    = createTeam("Bayern München",    1900, "Múnich");
            Team roma      = createTeam("AS Roma",           1927, "Roma");
            Team inter     = createTeam("Inter de Milán",    1908, "Milán");
            Team juventus  = createTeam("Juventus FC",       1897, "Turín");
            Team napoli    = createTeam("SSC Nápoles",       1926, "Nápoles");

            // Champions League — 8 equipos
            for (Team t : List.of(madrid, barca, city, liverpool, psg, bayern, roma, inter)) {
                tournamentService.addTeamToTournament(champions.getId(), t.getId());
            }
            // La Liga — 4 equipos
            for (Team t : List.of(madrid, barca, atletico, sevilla)) {
                tournamentService.addTeamToTournament(laliga.getId(), t.getId());
            }
            // Serie A — 4 equipos
            for (Team t : List.of(roma, inter, juventus, napoli)) {
                tournamentService.addTeamToTournament(serieA.getId(), t.getId());
            }

            System.out.println("✅ TORNEOS Y EQUIPOS CONFIGURADOS.");
        }

        // --- FASE 2: JUGADORES ---
        if (playerRepository.count() == 0) {
            System.out.println("🏃 RECLUTANDO JUGADORES...");

            Team madrid    = teamRepository.findByName("Real Madrid").get(0);
            Team barca     = teamRepository.findByName("FC Barcelona").get(0);
            Team atletico  = teamRepository.findByName("Atlético de Madrid").get(0);
            Team sevilla   = teamRepository.findByName("Sevilla FC").get(0);
            Team city      = teamRepository.findByName("Manchester City").get(0);
            Team liverpool = teamRepository.findByName("Liverpool FC").get(0);
            Team psg       = teamRepository.findByName("Paris Saint-Germain").get(0);
            Team bayern    = teamRepository.findByName("Bayern München").get(0);
            Team roma      = teamRepository.findByName("AS Roma").get(0);
            Team inter     = teamRepository.findByName("Inter de Milán").get(0);
            Team juventus  = teamRepository.findByName("Juventus FC").get(0);
            Team napoli    = teamRepository.findByName("SSC Nápoles").get(0);

            // Real Madrid
            createPlayer("Kylian",     "Mbappé",      "Delantero",  LocalDate.of(1998, 12, 20), 1.78, madrid);
            createPlayer("Vinícius",   "Júnior",      "Delantero",  LocalDate.of(2000,  7, 12), 1.76, madrid);
            createPlayer("Jude",       "Bellingham",  "Centrocampista", LocalDate.of(2003, 6, 29), 1.86, madrid);
            createPlayer("Luka",       "Modrić",      "Centrocampista", LocalDate.of(1985, 9,  9), 1.72, madrid);
            createPlayer("Thibaut",    "Courtois",    "Portero",    LocalDate.of(1992,  5, 11), 1.99, madrid);
            createPlayer("Éder",       "Militão",     "Defensa",    LocalDate.of(1998,  1, 18), 1.86, madrid);

            // FC Barcelona
            createPlayer("Robert",     "Lewandowski", "Delantero",  LocalDate.of(1988,  8, 21), 1.85, barca);
            createPlayer("Pedri",      "González",    "Centrocampista", LocalDate.of(2002, 11, 25), 1.74, barca);
            createPlayer("Gavi",       "Páez",        "Centrocampista", LocalDate.of(2004,  8,  5), 1.73, barca);
            createPlayer("Marc-André", "ter Stegen",  "Portero",    LocalDate.of(1992,  4, 30), 1.87, barca);
            createPlayer("Ronald",     "Araújo",      "Defensa",    LocalDate.of(1999,  3,  7), 1.88, barca);

            // Atlético de Madrid
            createPlayer("Antoine",    "Griezmann",   "Delantero",  LocalDate.of(1991,  3, 21), 1.76, atletico);
            createPlayer("Álvaro",     "Morata",      "Delantero",  LocalDate.of(1992, 10, 23), 1.89, atletico);
            createPlayer("Rodrigo",    "De Paul",     "Centrocampista", LocalDate.of(1994,  5, 24), 1.80, atletico);
            createPlayer("Jan",        "Oblak",       "Portero",    LocalDate.of(1993,  1,  7), 1.88, atletico);

            // Sevilla FC
            createPlayer("Jesús",      "Navas",       "Defensa",    LocalDate.of(1985,  11, 21), 1.72, sevilla);
            createPlayer("Youssef",    "En-Nesyri",   "Delantero",  LocalDate.of(1997,   6,  1), 1.89, sevilla);

            // Manchester City
            createPlayer("Erling",     "Haaland",     "Delantero",  LocalDate.of(2000,  7, 21), 1.94, city);
            createPlayer("Kevin",      "De Bruyne",   "Centrocampista", LocalDate.of(1991, 6, 28), 1.81, city);
            createPlayer("Phil",       "Foden",       "Centrocampista", LocalDate.of(2000,  5, 28), 1.71, city);
            createPlayer("Ederson",    "Moraes",      "Portero",    LocalDate.of(1993,  8, 17), 1.88, city);

            // Liverpool FC
            createPlayer("Mohamed",    "Salah",       "Delantero",  LocalDate.of(1992,  6, 15), 1.75, liverpool);
            createPlayer("Darwin",     "Núñez",       "Delantero",  LocalDate.of(2000,  6, 24), 1.87, liverpool);
            createPlayer("Virgil",     "van Dijk",    "Defensa",    LocalDate.of(1991,  7,  8), 1.93, liverpool);
            createPlayer("Alisson",    "Becker",      "Portero",    LocalDate.of(1992, 10,  2), 1.91, liverpool);

            // Paris Saint-Germain
            createPlayer("Ousmane",    "Dembélé",     "Delantero",  LocalDate.of(1997,  5, 15), 1.78, psg);
            createPlayer("Marquinhos", "Aoas Corrêa", "Defensa",    LocalDate.of(1994,  5, 14), 1.83, psg);

            // Bayern München
            createPlayer("Harry",      "Kane",        "Delantero",  LocalDate.of(1993,  7, 28), 1.88, bayern);
            createPlayer("Jamal",      "Musiala",     "Centrocampista", LocalDate.of(2003, 2, 26), 1.80, bayern);
            createPlayer("Manuel",     "Neuer",       "Portero",    LocalDate.of(1986,  3, 27), 1.93, bayern);

            // AS Roma
            createPlayer("Paulo",      "Dybala",      "Delantero",  LocalDate.of(1993, 11, 15), 1.77, roma);
            createPlayer("Lorenzo",    "Pellegrini",  "Centrocampista", LocalDate.of(1996, 6, 19), 1.86, roma);
            createPlayer("Romelu",     "Lukaku",      "Delantero",  LocalDate.of(1993,  5, 13), 1.90, roma);

            // Inter de Milán
            createPlayer("Lautaro",    "Martínez",    "Delantero",  LocalDate.of(1997,  8, 22), 1.74, inter);
            createPlayer("Nicolò",     "Barella",     "Centrocampista", LocalDate.of(1997, 2, 7), 1.72, inter);
            createPlayer("Yann",       "Sommer",      "Portero",    LocalDate.of(1988, 12, 17), 1.83, inter);

            // Juventus FC
            createPlayer("Dušan",      "Vlahović",    "Delantero",  LocalDate.of(2000,  1, 28), 1.90, juventus);
            createPlayer("Federico",   "Chiesa",      "Delantero",  LocalDate.of(1997, 10, 25), 1.75, juventus);
            createPlayer("Adrien",     "Rabiot",      "Centrocampista", LocalDate.of(1995, 4, 3), 1.88, juventus);

            // SSC Nápoles
            createPlayer("Victor",     "Osimhen",     "Delantero",  LocalDate.of(1998, 12, 29), 1.85, napoli);
            createPlayer("Khvicha",    "Kvaratskhelia","Delantero",  LocalDate.of(2001,  2, 12), 1.83, napoli);
            createPlayer("Piotr",      "Zieliński",   "Centrocampista", LocalDate.of(1994, 5, 20), 1.80, napoli);

            System.out.println("✅ PLANTILLAS COMPLETADAS.");
        }

        // --- FASE 3: ÁRBITROS Y PARTIDOS ---
        if (matchRepository.count() == 0) {
            System.out.println("🏟️ PROGRAMANDO CALENDARIO...");

            Referee collina   = createReferee("Pierluigi", "Collina",   "REF-ITA-001");
            Referee marciniak = createReferee("Szymon",    "Marciniak", "REF-POL-002");
            Referee lahoz     = createReferee("Antonio",   "Lahoz",     "REF-ESP-003");
            Referee oliver    = createReferee("Michael",   "Oliver",    "REF-ENG-004");
            Referee orsato    = createReferee("Daniele",   "Orsato",    "REF-ITA-005");
            Referee taylor    = createReferee("Anthony",   "Taylor",    "REF-ENG-006");

            Tournament champions = tournamentRepository.findByName("UEFA Champions League 2025/26").get(0);
            Tournament laliga    = tournamentRepository.findByName("La Liga 2025/26").get(0);
            Tournament serieA    = tournamentRepository.findByName("Serie A 2025/26").get(0);

            Team madrid    = teamRepository.findByName("Real Madrid").get(0);
            Team barca     = teamRepository.findByName("FC Barcelona").get(0);
            Team atletico  = teamRepository.findByName("Atlético de Madrid").get(0);
            Team sevilla   = teamRepository.findByName("Sevilla FC").get(0);
            Team city      = teamRepository.findByName("Manchester City").get(0);
            Team liverpool = teamRepository.findByName("Liverpool FC").get(0);
            Team psg       = teamRepository.findByName("Paris Saint-Germain").get(0);
            Team bayern    = teamRepository.findByName("Bayern München").get(0);
            Team roma      = teamRepository.findByName("AS Roma").get(0);
            Team inter     = teamRepository.findByName("Inter de Milán").get(0);
            Team juventus  = teamRepository.findByName("Juventus FC").get(0);
            Team napoli    = teamRepository.findByName("SSC Nápoles").get(0);

            // ====== CHAMPIONS LEAGUE — Fase de grupos ======
            // Jornada 1
            createMatch(madrid,   city,      champions, collina,   3, 1, LocalDateTime.of(2025, 9, 16, 21,  0), MatchStatus.PLAYED,    "Santiago Bernabéu, Madrid");
            createMatch(barca,    psg,       champions, marciniak, 2, 2, LocalDateTime.of(2025, 9, 16, 21,  0), MatchStatus.PLAYED,    "Estadio Olímpico, Barcelona");
            createMatch(liverpool,bayern,    champions, oliver,    3, 2, LocalDateTime.of(2025, 9, 17, 20, 45), MatchStatus.PLAYED,    "Anfield, Liverpool");
            createMatch(inter,    roma,      champions, orsato,    1, 1, LocalDateTime.of(2025, 9, 17, 20, 45), MatchStatus.PLAYED,    "Giuseppe Meazza, Milán");

            // Jornada 2
            createMatch(city,     liverpool, champions, taylor,    2, 2, LocalDateTime.of(2025, 10, 1, 21,  0), MatchStatus.PLAYED,    "Etihad Stadium, Manchester");
            createMatch(psg,      inter,     champions, lahoz,     1, 0, LocalDateTime.of(2025, 10, 1, 21,  0), MatchStatus.PLAYED,    "Parc des Princes, París");
            createMatch(madrid,   barca,     champions, marciniak, 2, 1, LocalDateTime.of(2025, 10, 2, 21,  0), MatchStatus.PLAYED,    "Santiago Bernabéu, Madrid");
            createMatch(bayern,   roma,      champions, collina,   4, 0, LocalDateTime.of(2025, 10, 2, 20, 45), MatchStatus.PLAYED,    "Allianz Arena, Múnich");

            // Jornada 3
            createMatch(liverpool, madrid,   champions, orsato,    1, 3, LocalDateTime.of(2025, 10, 22, 21, 0), MatchStatus.PLAYED,    "Anfield, Liverpool");
            createMatch(roma,      psg,      champions, oliver,    2, 1, LocalDateTime.of(2025, 10, 22, 21, 0), MatchStatus.PLAYED,    "Estadio Olímpico, Roma");
            createMatch(barca,     inter,    champions, taylor,    3, 1, LocalDateTime.of(2025, 10, 23, 21, 0), MatchStatus.PLAYED,    "Estadio Olímpico, Barcelona");
            createMatch(city,      bayern,   champions, lahoz,     1, 2, LocalDateTime.of(2025, 10, 23, 20,45), MatchStatus.PLAYED,    "Etihad Stadium, Manchester");

            // Jornada 4 — pendientes
            createMatch(madrid,   inter,     champions, collina,   0, 0, LocalDateTime.of(2025, 11, 5, 21,  0), MatchStatus.SCHEDULED, "Santiago Bernabéu, Madrid");
            createMatch(psg,      liverpool, champions, marciniak, 0, 0, LocalDateTime.of(2025, 11, 5, 21,  0), MatchStatus.SCHEDULED, "Parc des Princes, París");
            createMatch(barca,    city,      champions, oliver,    0, 0, LocalDateTime.of(2025, 11, 6, 21,  0), MatchStatus.SCHEDULED, "Estadio Olímpico, Barcelona");
            createMatch(roma,     bayern,    champions, orsato,    0, 0, LocalDateTime.of(2025, 11, 6, 21,  0), MatchStatus.SCHEDULED, "Estadio Olímpico, Roma");

            // ====== LA LIGA — Jornadas ======
            createMatch(madrid,   sevilla,   laliga,    lahoz,     4, 1, LocalDateTime.of(2025, 8, 17, 21,  0), MatchStatus.PLAYED,    "Santiago Bernabéu, Madrid");
            createMatch(barca,    atletico,  laliga,    taylor,    2, 2, LocalDateTime.of(2025, 8, 17, 21,  0), MatchStatus.PLAYED,    "Estadio Olímpico, Barcelona");
            createMatch(atletico, madrid,    laliga,    oliver,    1, 2, LocalDateTime.of(2025, 8, 31, 21,  0), MatchStatus.PLAYED,    "Metropolitano, Madrid");
            createMatch(sevilla,  barca,     laliga,    collina,   0, 3, LocalDateTime.of(2025, 8, 31, 21,  0), MatchStatus.PLAYED,    "Ramón Sánchez-Pizjuán, Sevilla");
            createMatch(madrid,   atletico,  laliga,    marciniak, 3, 1, LocalDateTime.of(2025, 9, 21, 21,  0), MatchStatus.PLAYED,    "Santiago Bernabéu, Madrid");
            createMatch(barca,    sevilla,   laliga,    orsato,    4, 0, LocalDateTime.of(2025, 9, 21, 21,  0), MatchStatus.PLAYED,    "Estadio Olímpico, Barcelona");
            createMatch(atletico, sevilla,   laliga,    lahoz,     2, 0, LocalDateTime.of(2025, 10, 5, 18, 30), MatchStatus.PLAYED,    "Metropolitano, Madrid");
            createMatch(sevilla,  madrid,    laliga,    taylor,    0, 0, LocalDateTime.of(2025, 10, 19, 16,15), MatchStatus.SCHEDULED, "Ramón Sánchez-Pizjuán, Sevilla");
            createMatch(atletico, barca,     laliga,    oliver,    0, 0, LocalDateTime.of(2025, 10, 26, 21,  0), MatchStatus.SCHEDULED, "Metropolitano, Madrid");

            // ====== SERIE A — Jornadas ======
            createMatch(inter,    napoli,    serieA,    collina,   2, 1, LocalDateTime.of(2025, 8, 25, 20, 45), MatchStatus.PLAYED,    "Giuseppe Meazza, Milán");
            createMatch(roma,     juventus,  serieA,    orsato,    1, 1, LocalDateTime.of(2025, 8, 25, 20, 45), MatchStatus.PLAYED,    "Estadio Olímpico, Roma");
            createMatch(napoli,   juventus,  serieA,    marciniak, 2, 3, LocalDateTime.of(2025, 9,  1, 20, 45), MatchStatus.PLAYED,    "Diego Armando Maradona, Nápoles");
            createMatch(juventus, inter,     serieA,    oliver,    0, 1, LocalDateTime.of(2025, 9, 14, 18,  0), MatchStatus.PLAYED,    "Allianz Stadium, Turín");
            createMatch(inter,    roma,      serieA,    lahoz,     3, 0, LocalDateTime.of(2025, 9, 28, 20, 45), MatchStatus.PLAYED,    "Giuseppe Meazza, Milán");
            createMatch(napoli,   roma,      serieA,    taylor,    1, 2, LocalDateTime.of(2025, 10,  5, 15,  0), MatchStatus.PLAYED,    "Diego Armando Maradona, Nápoles");
            createMatch(juventus, napoli,    serieA,    collina,   2, 1, LocalDateTime.of(2025, 10, 19, 20, 45), MatchStatus.PLAYED,    "Allianz Stadium, Turín");
            createMatch(roma,     inter,     serieA,    orsato,    0, 0, LocalDateTime.of(2025, 11,  2, 20, 45), MatchStatus.SCHEDULED, "Estadio Olímpico, Roma");
            createMatch(napoli,   inter,     serieA,    marciniak, 0, 0, LocalDateTime.of(2025, 11,  9, 15,  0), MatchStatus.SCHEDULED, "Diego Armando Maradona, Nápoles");
            createMatch(juventus, roma,      serieA,    taylor,    0, 0, LocalDateTime.of(2025, 11, 23, 20, 45), MatchStatus.SCHEDULED, "Allianz Stadium, Turín");

            System.out.println("✅ CALENDARIO GENERADO.");
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private Tournament createTournament(String name, LocalDate date, String desc) {
        Tournament t = new Tournament();
        t.setName(name);
        t.setStartDate(date);
        t.setDescription(desc);
        return tournamentService.saveTournament(t);
    }

    private Team createTeam(String name, int year, String city) {
        Team t = new Team();
        t.setName(name);
        t.setFoundationYear(year);
        t.setCity(city);
        return teamRepository.save(t);
    }

    private void createPlayer(String name, String surname, String pos, LocalDate birth, double height, Team team) {
        Player p = new Player();
        p.setName(name);
        p.setSurname(surname);
        p.setPosition(pos);
        p.setBirthDate(birth);
        p.setHeight(height);
        p = playerService.savePlayer(p);
        playerService.assignPlayerToTeam(p.getId(), team.getId());
    }

    private Referee createReferee(String name, String surname, String code) {
        Referee r = new Referee();
        r.setName(name);
        r.setSurname(surname);
        r.setRefereeCode(code);
        return refereeService.saveReferee(r);
    }

    private void createMatch(Team home, Team away, Tournament t, Referee r,
                              int hScore, int aScore, LocalDateTime date,
                              MatchStatus status, String loc) {
        Match m = new Match();
        m.setHomeTeam(home);
        m.setAwayTeam(away);
        m.setTournament(t);
        m.setReferee(r);
        m.setHomeScore(hScore);
        m.setAwayScore(aScore);
        m.setMatchDate(date);
        m.setStatus(status);
        m.setLocation(loc);
        matchService.saveMatch(m);
    }

    private void createUsers() {
        if (userService.findByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole("ADMIN");
            userService.saveUser(admin);
        }
        if (userService.findByUsername("pablo") == null) {
            User user = new User();
            user.setUsername("pablo");
            user.setPassword(passwordEncoder.encode("1234"));
            user.setRole("USER");
            userService.saveUser(user);
        }
    }
}
