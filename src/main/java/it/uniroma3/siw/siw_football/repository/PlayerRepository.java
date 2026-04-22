package it.uniroma3.siw.siw_football.repository;

import it.uniroma3.siw.siw_football.model.Player;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface PlayerRepository extends CrudRepository<Player, Long> {

    List<Player> findByTeamId(Long teamId);
    
}