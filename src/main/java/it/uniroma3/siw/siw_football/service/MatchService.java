package it.uniroma3.siw.siw_football.service;

import it.uniroma3.siw.siw_football.model.Match;
import it.uniroma3.siw.siw_football.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;

    @Transactional
    public Match saveMatch(Match match) {
        return matchRepository.save(match);
    }
}