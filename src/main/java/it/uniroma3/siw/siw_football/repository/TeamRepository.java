package it.uniroma3.siw.siw_football.repository;

import it.uniroma3.siw.siw_football.model.Team;
import org.springframework.data.repository.CrudRepository;

public interface TeamRepository extends CrudRepository<Team , Long> {
    
}
