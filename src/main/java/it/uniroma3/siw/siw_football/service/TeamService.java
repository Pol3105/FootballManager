package it.uniroma3.siw.siw_football.service;

import it.uniroma3.siw.siw_football.model.Match;
import it.uniroma3.siw.siw_football.model.Player;
import it.uniroma3.siw.siw_football.model.Team;
import it.uniroma3.siw.siw_football.model.Tournament;
import it.uniroma3.siw.siw_football.repository.MatchRepository;
import it.uniroma3.siw.siw_football.repository.TeamRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchRepository matchRepository;

    /**
     * Listar todos los equipos
     */
    @Transactional(readOnly = true)
    public List<Team> findAll() {
        return (List<Team>) teamRepository.findAll();
    }

    /**
     * Busco un team basandome en el ID
     */
    @Transactional(readOnly = true) // Optimización porque solo vamos a leer datos
    public Team findById(Long id) {
        return teamRepository.findById(id).orElse(null);
    }

    /**
     * Guardo un team en la BD
     */
    @Transactional
    public void save(Team team) {
        teamRepository.save(team);
    }

    /**
     * Elimino un team de la BD
     */
    @Transactional
    public void deleteById(Long id) {
        Team team = teamRepository.findById(id).orElse(null);
        if (team != null) {
            for (Tournament t : team.getTournaments()) {
                t.getTeams().remove(team);
            }
            team.getTournaments().clear();

            for (Player p : team.getPlayers()) {
                p.setTeam(null);
            }

            List<Match> matches = matchRepository.findByHomeTeamOrAwayTeam(team, team);
            matchRepository.deleteAll(matches);

            
            teamRepository.delete(team);
        }
    }
}