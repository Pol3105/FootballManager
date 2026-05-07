package it.uniroma3.siw.siw_football.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import it.uniroma3.siw.siw_football.model.Comment;
import it.uniroma3.siw.siw_football.model.Match;
import it.uniroma3.siw.siw_football.model.Tournament;
import it.uniroma3.siw.siw_football.model.User;
import it.uniroma3.siw.siw_football.service.CommentService;
import it.uniroma3.siw.siw_football.service.MatchService;
import it.uniroma3.siw.siw_football.service.RefereeService;
import it.uniroma3.siw.siw_football.service.TournamentService;
import it.uniroma3.siw.siw_football.service.UserService;

@Controller
public class MatchController {

    @Autowired private MatchService matchService;
    @Autowired private TournamentService tournamentService;
    @Autowired private RefereeService refereeService;
    @Autowired private CommentService commentService;
    @Autowired private UserService userService;

    

    // 1. Formulario para Nuevo Partido (vinculado a un Torneo)
    @GetMapping("/admin/tournament/{tournamentId}/match/new")
    public String showNewMatchForm(@PathVariable("tournamentId") Long tournamentId, Model model) {
        Tournament tournament = tournamentService.findById(tournamentId);
        
        Match match = new Match();
        match.setTournament(tournament); // Pre-asignamos el torneo
        
        model.addAttribute("match", match);
        model.addAttribute("tournament", tournament);
        model.addAttribute("teams", tournament.getTeams()); // Solo equipos del torneo
        model.addAttribute("referees", refereeService.findAll()); // Todos los árbitros
        return "admin/form-match";
    }

    // 2. Formulario para Editar Partido
    @GetMapping("/admin/match/edit/{id}")
    public String showEditMatchForm(@PathVariable("id") Long id, Model model) {
        Match match = matchService.findById(id);
        if (match != null) {
            model.addAttribute("match", match);
            model.addAttribute("tournament", match.getTournament());
            model.addAttribute("teams", match.getTournament().getTeams());
            model.addAttribute("referees", refereeService.findAll());
            return "admin/form-match";
        }
        return "redirect:/";
    }

    @PostMapping("/admin/match/save")
    public String saveMatch(@ModelAttribute("match") Match match, RedirectAttributes redirectAttributes) {
        if (match.getHomeTeam().equals(match.getAwayTeam())) {
            return "redirect:/admin/tournament/" + match.getTournament().getId() + "/match/new?error=sameTeam";
        }
        
        matchService.saveMatch(match);
        return "redirect:/tournament/" + match.getTournament().getId();
    }

    // Detalle público del partido con comentarios (Req 4.1 + 4.2)
    @GetMapping("/match/{id}")
    public String showMatchDetails(@PathVariable("id") Long id, Model model) {
        Match match = matchService.findById(id);
        if (match == null) return "redirect:/";
        model.addAttribute("match", match);
        model.addAttribute("comments", commentService.findByMatchId(id));
        return "match-details";
    }

    // Req 4.2: Insertar comentario en partida (solo usuarios autenticados)
    @PostMapping("/match/{id}/comment")
    public String addComment(@PathVariable("id") Long id,
                             @RequestParam("content") String content,
                             Authentication authentication) {
        Match match = matchService.findById(id);
        User user = userService.findByUsername(authentication.getName());
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setMatch(match);
        comment.setUser(user);
        commentService.saveComment(comment);
        return "redirect:/match/" + id;
    }

    // Borrar comentario (solo ADMIN)
    @GetMapping("/admin/comment/delete/{id}")
    public String deleteComment(@PathVariable("id") Long id) {
        Comment comment = commentService.findById(id);
        Long matchId = comment.getMatch().getId();
        commentService.deleteById(id);
        return "redirect:/match/" + matchId;
    }

    // 4. Borrar partido
    @GetMapping("/admin/match/delete/{id}")
    public String deleteMatch(@PathVariable("id") Long id) {
        Match match = matchService.findById(id);
        Long tournamentId = match.getTournament().getId();
        matchService.deleteById(id);
        return "redirect:/tournament/" + tournamentId;
    }
}