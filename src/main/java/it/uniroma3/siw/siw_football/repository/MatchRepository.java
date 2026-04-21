package it.uniroma3.siw.siw_football.repository;

import it.uniroma3.siw.siw_football.model.Match;
import org.springframework.data.repository.CrudRepository;

public interface MatchRepository extends CrudRepository<Match, Long> {
}
