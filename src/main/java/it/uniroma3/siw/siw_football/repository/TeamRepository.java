package it.uniroma3.siw.siw_football.repository;

import it.uniroma3.siw.siw_football.model.Team;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface TeamRepository extends CrudRepository<Team , Long> {
    List<Team> findByName(String name);
}
