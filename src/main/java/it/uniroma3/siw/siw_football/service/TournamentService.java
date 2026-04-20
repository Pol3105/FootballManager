package it.uniroma3.siw.siw_football.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.siw_football.model.Team;
import it.uniroma3.siw.siw_football.model.Tournament;
import it.uniroma3.siw.siw_football.repository.TeamRepository;
import it.uniroma3.siw.siw_football.repository.TournamentRepository;

@Service
public class TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TeamRepository teamRepository;

    /**
     * Guarda un torneo en la base de datos.
     */
    @Transactional
    public Tournament saveTournament(Tournament tournament) {
        return tournamentRepository.save(tournament);
    }

    /**
     * Recupera todos los torneos.
     */
    public Iterable<Tournament> findAll() {
        return tournamentRepository.findAll();
    }

    /**
     * Lógica compleja: Añadir un equipo existente a un torneo.∫
     */
    @Transactional
    public void addTeamToTournament(Long tournamentId, Long teamId) {
        Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);
        Team team = teamRepository.findById(teamId).orElse(null);

        if (tournament != null && team != null) {
            team.setTournament(tournament); // Establecemos la relación
            tournament.getTeams().add(team); // Actualizamos la lista del torneo
        }
    }
}
