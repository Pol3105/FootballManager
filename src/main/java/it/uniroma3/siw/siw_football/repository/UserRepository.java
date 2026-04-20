package it.uniroma3.siw.siw_football.repository;

import it.uniroma3.siw.siw_football.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    // Spring creará automáticamente: save(), findById(), findAll(), delete()...
}