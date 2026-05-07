package it.uniroma3.siw.siw_football.repository;

import it.uniroma3.siw.siw_football.model.Comment;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CommentRepository extends CrudRepository<Comment, Long> {
    List<Comment> findByMatchId(Long matchId);
}
