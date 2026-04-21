package it.uniroma3.siw.siw_football.service;

import it.uniroma3.siw.siw_football.model.Player;
import it.uniroma3.siw.siw_football.model.Team;
import it.uniroma3.siw.siw_football.repository.PlayerRepository;
import it.uniroma3.siw.siw_football.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Transactional
    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }

    public Iterable<Player> findAll() {
        return playerRepository.findAll();
    }

    /**
     * Lógica de Fichaje: Asigna un jugador existente a un equipo.
     */
    @Transactional
    public void assignPlayerToTeam(Long playerId, Long teamId) {
        Player player = playerRepository.findById(playerId).orElse(null);
        Team team = teamRepository.findById(teamId).orElse(null);

        if (player != null && team != null) {
            player.setTeam(team);
            team.getPlayers().add(player);
        }
    }
}