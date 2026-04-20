package it.uniroma3.siw.siw_football.config;


import it.uniroma3.siw.siw_football.model.Tournament;
import it.uniroma3.siw.siw_football.model.Team;
import it.uniroma3.siw.siw_football.service.TournamentService;
import it.uniroma3.siw.siw_football.repository.TeamRepository;
import it.uniroma3.siw.siw_football.repository.TournamentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private TeamRepository teamRepository; // Usamos el repo directo para crear los equipos iniciales

    @Autowired
    private TournamentRepository tournamentRepository;

    @Override
    public void run(String... args) throws Exception {
        
        // Comprobamos si la base de datos está vacía usando el método count() del repositorio
        if (tournamentRepository.count() == 0) {
            System.out.println("⚽ LA BD ESTÁ VACÍA. INICIANDO INYECCIÓN DE DATOS...");

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

            System.out.println("✅ DATOS INYECTADOS CON ÉXITO.");
        } else {
            System.out.println("👍 LOS DATOS YA EXISTEN. Omitiendo inyección.");
        }
    }
}