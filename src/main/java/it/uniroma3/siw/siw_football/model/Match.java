package it.uniroma3.siw.siw_football.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime matchDate;

    private Integer homeScore;
    private Integer awayScore;

    // Relación 1: Equipo Local
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id") // Forzamos el nombre de la columna en BD
    private Team homeTeam;

    // Relación 2: Equipo Visitante
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id") 
    private Team awayTeam;

    // Relación 3: El Árbitro del partido
    @ManyToOne(fetch = FetchType.LAZY)
    private Referee referee;

    // Relación 3: El Árbitro del partido
    @ManyToOne(fetch = FetchType.LAZY)
    private Tournament tournament;

    public Match() {}

    // --- Getters y Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getMatchDate() { return matchDate; }
    public void setMatchDate(LocalDateTime matchDate) { this.matchDate = matchDate; }

    public Integer getHomeScore() { return homeScore; }
    public void setHomeScore(Integer homeScore) { this.homeScore = homeScore; }

    public Integer getAwayScore() { return awayScore; }
    public void setAwayScore(Integer awayScore) { this.awayScore = awayScore; }

    public Team getHomeTeam() { return homeTeam; }
    public void setHomeTeam(Team homeTeam) { this.homeTeam = homeTeam; }

    public Team getAwayTeam() { return awayTeam; }
    public void setAwayTeam(Team awayTeam) { this.awayTeam = awayTeam; }

    public Referee getReferee() { return referee; }
    public void setReferee(Referee referee) { this.referee = referee; }

    public Tournament getTournament() { return tournament; }
    public void setTournament(Tournament tournament) { this.tournament = tournament; }

}