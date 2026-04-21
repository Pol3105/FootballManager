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
     * Busco un torneo basandome en el ID
     */
    @Transactional(readOnly = true)
    public Tournament findById(Long id) {
        return tournamentRepository.findById(id).orElse(null);
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

    /**
     * Lógica : Añadir un torneo.∫
     */
    @Transactional
    public void save(Tournament tournament) {
        tournamentRepository.save(tournament);
    }

    /**
     * Lógica : Eliminar un torneo por su ID.∫
     */
    @Transactional
    public void deleteById(Long id) {
        tournamentRepository.deleteById(id);
    }


}
