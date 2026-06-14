package it.uniroma3.siw.siw_football.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;

import it.uniroma3.siw.siw_football.model.Player;
import it.uniroma3.siw.siw_football.model.Team;
import it.uniroma3.siw.siw_football.service.PlayerService;
import it.uniroma3.siw.siw_football.service.TeamService;

@Controller
public class PlayerController {
    @Autowired private PlayerService playerService;
    @Autowired private TeamService teamService;

    @GetMapping("/admin/player/new")
    public String showNewPlayerForm(@RequestParam(required = false) Long teamId, Model model) {
        Player player = new Player();
        if (teamId != null) {
            Team team = teamService.findById(teamId);
            if (team != null) player.setTeam(team);
        }
        model.addAttribute("player", player);
        model.addAttribute("teams", teamService.findAll());
        return "admin/form-player";
    }

    @PostMapping("/admin/player/save")
    public String savePlayer(@Valid @ModelAttribute("player") Player player,
                             BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("teams", teamService.findAll());
            return "admin/form-player";
        }
        playerService.savePlayer(player);
        return "redirect:/players";
    }
    
    @GetMapping("/players")
    public String listPlayers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "6") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) Long teamId,
            Model model) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Player> playersPage = playerService.search(q, position, teamId, pageable);
        
        model.addAttribute("playersPage", playersPage);
        model.addAttribute("players", playersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", playersPage.getTotalPages());
        model.addAttribute("teams", teamService.findAll());
        model.addAttribute("q", q != null ? q : "");
        model.addAttribute("position", position != null ? position : "");
        model.addAttribute("teamId", teamId);
        return "players";
    }

    @GetMapping("/admin/player/edit/{id}")
    public String showEditPlayerForm(@PathVariable("id") Long id, Model model) {
        Player player = playerService.findById(id);
        if (player != null) {
            model.addAttribute("player", player);
            model.addAttribute("teams", teamService.findAll());
            return "admin/form-player";
        }
        return "redirect:/players";
    }

    // ELIMINAR JUGADOR (El otro que te daba 404)
    @GetMapping("/admin/player/delete/{id}")
    public String deletePlayer(@PathVariable("id") Long id) {
        playerService.deleteById(id);
        return "redirect:/players";
    }
    
}
