package it.uniroma3.siw.siw_football.service;

import it.uniroma3.siw.siw_football.model.Comment;
import it.uniroma3.siw.siw_football.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Transactional
    public Comment saveComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public List<Comment> findByMatchId(Long matchId) {
        return commentRepository.findByMatchId(matchId);
    }

    public Comment findById(Long id) {
        return commentRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deleteById(Long id) {
        commentRepository.deleteById(id);
    }

    @Transactional
    public boolean updateComment(Long commentId, String content, String username) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null || !comment.getUser().getUsername().equals(username)) return false;
        comment.setContent(content);
        return true;
    }
}
