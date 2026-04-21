package it.uniroma3.siw.siw_football.service;

import it.uniroma3.siw.siw_football.model.Team;
import it.uniroma3.siw.siw_football.model.Tournament;
import it.uniroma3.siw.siw_football.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    /**
     * Busco un team basandome en el ID
     */
    @Transactional(readOnly = true) // Optimización porque solo vamos a leer datos
    public Team findById(Long id) {
        return teamRepository.findById(id).orElse(null);
    }

    
}