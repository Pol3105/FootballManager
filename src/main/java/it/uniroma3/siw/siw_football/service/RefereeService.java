package it.uniroma3.siw.siw_football.service;

import it.uniroma3.siw.siw_football.model.Match;
import it.uniroma3.siw.siw_football.model.Player;
import it.uniroma3.siw.siw_football.model.Referee;
import it.uniroma3.siw.siw_football.model.Team;
import it.uniroma3.siw.siw_football.model.Tournament;
import it.uniroma3.siw.siw_football.repository.MatchRepository;
import it.uniroma3.siw.siw_football.repository.RefereeRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefereeService {

    @Autowired
    private RefereeRepository refereeRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Transactional
    public Referee saveReferee(Referee referee) {
        return refereeRepository.save(referee);
    }

    public Referee findById(Long id) {
        return refereeRepository.findById(id).orElse(null);
    }

    public void deleteById(Long id) {
        List<Match> matchesWithThisReferee = matchRepository.findByRefereeId(id);
        matchRepository.deleteAll(matchesWithThisReferee);
        refereeRepository.deleteById(id);
    }

    public List<Referee> findAll() {
        return (List<Referee>) refereeRepository.findAll();
    }
}