package it.uniroma3.siw.siw_football.repository;

import it.uniroma3.siw.siw_football.model.Player;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    List<Player> findByTeamId(Long teamId);

    @Query("SELECT p FROM Player p LEFT JOIN p.team t WHERE " +
           "(:q = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "   OR LOWER(p.surname) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "AND (:position = '' OR p.position = :position) " +
           "ORDER BY p.surname ASC, p.name ASC")
    org.springframework.data.domain.Page<Player> search(@Param("q") String q, @Param("position") String position, org.springframework.data.domain.Pageable pageable);
}