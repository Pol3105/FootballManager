package it.uniroma3.siw.siw_football.repository;


import it.uniroma3.siw.siw_football.model.Referee;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RefereeRepository extends ListCrudRepository<Referee, Long>, PagingAndSortingRepository<Referee, Long> {

    boolean existsByRefereeCode(String refereeCode);
    boolean existsByRefereeCodeAndIdNot(String refereeCode, Long id);
}