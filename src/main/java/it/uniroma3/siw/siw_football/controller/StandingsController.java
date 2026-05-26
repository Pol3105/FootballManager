package it.uniroma3.siw.siw_football.controller;

import it.uniroma3.siw.siw_football.model.StandingEntry;
import it.uniroma3.siw.siw_football.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StandingsController {

    @Autowired
    private TournamentService tournamentService;

    @GetMapping("/tournament/{id}/standings")
    public List<StandingEntry> getStandings(@PathVariable Long id) {
        return tournamentService.computeStandings(id);
    }
}
