package it.uniroma3.siw.siw_football.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.siw_football.model.Match;
import it.uniroma3.siw.siw_football.model.MatchStatus;
import it.uniroma3.siw.siw_football.model.StandingEntry;
import it.uniroma3.siw.siw_football.model.Team;
import it.uniroma3.siw.siw_football.model.Tournament;
import it.uniroma3.siw.siw_football.repository.MatchRepository;
import it.uniroma3.siw.siw_football.repository.TeamRepository;
import it.uniroma3.siw.siw_football.repository.TournamentRepository;


@Service
public class TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchRepository matchRepository;

    /**
     * Guarda un torneo en la base de datos.
     */
    @Transactional
    public Tournament saveTournament(Tournament tournament) {
        return tournamentRepository.save(tournament);
    }

    /**
     * Busco un torneo basandome en el ID
     */
    @Transactional(readOnly = true)
    public Tournament findById(Long id) {
        return tournamentRepository.findById(id).orElse(null);
    }

    /**
     * Recupera todos los torneos.
     */
    public Iterable<Tournament> findAll() {
        return tournamentRepository.findAll();
    }

    /**
     * Lógica compleja: Añadir un equipo existente a un torneo
     */
    @Transactional
    public void addTeamToTournament(Long tournamentId, Long teamId) {
        Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);
        Team team = teamRepository.findById(teamId).orElse(null);

        if (tournament != null && team != null) {
            if (!team.getTournaments().contains(tournament)) {
                team.getTournaments().add(tournament);
            }

            if (!tournament.getTeams().contains(team)) {
                tournament.getTeams().add(team);
            }
        }
    }

    /**
     * Lógica compleja: Quitar un equipo de un torneo
     */
    @Transactional
    public void removeTeamFromTournament(Long tournamentId, Long teamId) {
        Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);
        Team team = teamRepository.findById(teamId).orElse(null);
        if (tournament != null && team != null) {

            List<Match> matchesToDelete = matchRepository.findByTournamentAndTeam(tournament, team);

            matchRepository.deleteAll(matchesToDelete);

            tournament.getTeams().remove(team);
            team.getTournaments().remove(tournament);

            tournamentRepository.save(tournament);
        }
    }

    /**
     * Lógica : Añadir un torneo.∫
     */
    @Transactional
    public void save(Tournament tournament) {
        tournamentRepository.save(tournament);
    }

    /**
     * Guarda el torneo y sincroniza los equipos participantes.
     * El lado propietario de la relación es Team.tournaments, así que hay que
     * actualizar cada equipo (no basta con Tournament.teams, que es el lado inverso).
     */
    @Transactional
    public void saveWithTeams(Tournament tournament, List<Long> teamIds) {
        Tournament saved = tournamentRepository.save(tournament);
        java.util.Set<Long> selected = (teamIds == null)
                ? java.util.Collections.emptySet()
                : new java.util.HashSet<>(teamIds);

        for (Team team : teamRepository.findAll()) {
            boolean shouldHave = selected.contains(team.getId());
            boolean has = team.getTournaments().contains(saved);
            if (shouldHave && !has) {
                team.getTournaments().add(saved);
                teamRepository.save(team);
            } else if (!shouldHave && has) {
                team.getTournaments().remove(saved);
                teamRepository.save(team);
            }
        }
    }

    /**
     * Lógica : Eliminar un torneo por su ID.∫
     */
    @Transactional
    public void deleteById(Long id) {
        Tournament tournament = tournamentRepository.findById(id).orElse(null);
        if (tournament != null) {
            for (Team team : tournament.getTeams()) {
                team.getTournaments().remove(tournament);
            }

            tournamentRepository.delete(tournament);
        }
    }

    @Transactional(readOnly = true)
    public Page<Match> findMatchesPaginated(Long tournamentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return matchRepository.findByTournamentIdOrderByMatchDateDesc(tournamentId, pageable);
    }

    @Transactional(readOnly = true)
    public List<StandingEntry> computeStandings(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);
        if (tournament == null) return List.of();

        // teamId -> [played, won, drawn, lost, goalsFor, goalsAgainst]
        Map<Long, int[]> stats = new HashMap<>();
        Map<Long, String> names = new HashMap<>();

        for (Team team : tournament.getTeams()) {
            stats.put(team.getId(), new int[6]);
            names.put(team.getId(), team.getName());
        }

        for (Match match : tournament.getMatches()) {
            if (match.getStatus() != MatchStatus.PLAYED) continue;

            Long hId = match.getHomeTeam().getId();
            Long aId = match.getAwayTeam().getId();
            int hg = match.getHomeScore() != null ? match.getHomeScore() : 0;
            int ag = match.getAwayScore() != null ? match.getAwayScore() : 0;

            int[] h = stats.computeIfAbsent(hId, k -> new int[6]);
            int[] a = stats.computeIfAbsent(aId, k -> new int[6]);

            h[0]++; a[0]++;         // played
            h[4] += hg; h[5] += ag; // home gf/ga
            a[4] += ag; a[5] += hg; // away gf/ga

            if (hg > ag)      { h[1]++; a[3]++; }  // home win
            else if (hg < ag) { a[1]++; h[3]++; }  // away win
            else              { h[2]++; a[2]++; }  // draw
        }

        return stats.entrySet().stream()
            .map(e -> {
                int[] s = e.getValue();
                return new StandingEntry(
                    e.getKey(), names.getOrDefault(e.getKey(), "?"),
                    s[0], s[1], s[2], s[3], s[4], s[5],
                    s[4] - s[5],
                    s[1] * 3 + s[2]
                );
            })
            .sorted(Comparator
                .<StandingEntry>comparingInt(StandingEntry::points)
                .thenComparingInt(StandingEntry::goalDiff)
                .thenComparingInt(StandingEntry::goalsFor)
                .reversed())
            .collect(Collectors.toList());
    }
}
