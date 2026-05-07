package it.uniroma3.siw.siw_football.service;

import it.uniroma3.siw.siw_football.model.User;
import it.uniroma3.siw.siw_football.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
