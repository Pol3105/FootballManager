package it.uniroma3.siw.siw_football.repository;

import it.uniroma3.siw.siw_football.model.Tournament;
import org.springframework.data.repository.CrudRepository;

public interface TournamentRepository extends CrudRepository<Tournament, Long> {
}
