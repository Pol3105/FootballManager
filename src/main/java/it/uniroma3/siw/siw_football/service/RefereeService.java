package it.uniroma3.siw.siw_football.service;

import it.uniroma3.siw.siw_football.model.Referee;
import it.uniroma3.siw.siw_football.repository.RefereeRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefereeService {

    @Autowired
    private RefereeRepository refereeRepository;

    @Transactional
    public Referee saveReferee(Referee referee) {
        return refereeRepository.save(referee);
    }

    public List<Referee> findAll() {
        return (List<Referee>) refereeRepository.findAll();
    }
}