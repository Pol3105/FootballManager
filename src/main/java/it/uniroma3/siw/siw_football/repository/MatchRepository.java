package it.uniroma3.siw.siw_football.repository;

import it.uniroma3.siw.siw_football.model.Match;
import it.uniroma3.siw.siw_football.model.Team;
import it.uniroma3.siw.siw_football.model.Tournament;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface MatchRepository extends CrudRepository<Match, Long> {
        @Query("SELECT m FROM Match m WHERE m.tournament = :tournament AND (m.homeTeam = :team OR m.awayTeam = :team)")
        List<Match> findByTournamentAndTeam(@Param("tournament") Tournament tournament, @Param("team") Team team);

        List<Match> findByHomeTeamOrAwayTeam(Team homeTeam, Team awayTeam);

        List<Match> findByRefereeId(Long refereeId);

        List<Match> findByTournamentId(Long tournamentId);

        @Query("SELECT m FROM Match m " +
           "LEFT JOIN FETCH m.comments c " +
           "LEFT JOIN FETCH m.homeTeam " +
           "LEFT JOIN FETCH m.awayTeam " +
           "WHERE m.id = :id")
        Optional<Match> findByIdWithComments(@Param("id") Long id);
}
