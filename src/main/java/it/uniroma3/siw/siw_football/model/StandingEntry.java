package it.uniroma3.siw.siw_football.model;

public record StandingEntry(
    Long teamId,
    String teamName,
    int played,
    int won,
    int drawn,
    int lost,
    int goalsFor,
    int goalsAgainst,
    int goalDiff,
    int points
) {}
