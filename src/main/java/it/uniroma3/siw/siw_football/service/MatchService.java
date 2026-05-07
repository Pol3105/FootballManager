package it.uniroma3.siw.siw_football.service;

import it.uniroma3.siw.siw_football.model.Match;
import it.uniroma3.siw.siw_football.model.Team;
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

    @Transactional
    public void deleteById(Long id) {
        matchRepository.deleteById(id);
    }

    public Match findById(Long id) {
        return matchRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true) // Sección 7: Operación de solo lectura
    public Match findByIdWithComments(Long id) {
        // El servicio coordina al repositorio[cite: 1]
        return matchRepository.findByIdWithComments(id).orElse(null);
    }

   
}