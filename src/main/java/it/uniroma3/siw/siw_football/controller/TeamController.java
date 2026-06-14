package it.uniroma3.siw.siw_football.controller;

import it.uniroma3.siw.siw_football.model.Player;
import it.uniroma3.siw.siw_football.model.Team;
import it.uniroma3.siw.siw_football.service.PlayerService;
import it.uniroma3.siw.siw_football.service.TeamService;
import it.uniroma3.siw.siw_football.service.TournamentService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;

@Controller
public class TeamController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private PlayerService playerService;

    @GetMapping("/teams")
    public String showAllTeams(Model model) {
        model.addAttribute("teams", teamService.findAll());
        model.addAttribute("tournaments", tournamentService.findAll());
        return "teams-list"; // Nueva página
    }

    @GetMapping("/team/{id}")
    public String showTeamDetails(@PathVariable("id") Long id, Model model) {
        Team team = teamService.findById(id);
        
        if (team != null) {
            model.addAttribute("team", team);
            return "team-details"; // Nos llevará al nuevo HTML
        }
        
        return "redirect:/"; // Si alguien pone un ID falso a mano, lo mandamos a la portada
    }

    @GetMapping("/team/{id}/players")
    public String showTeamRoster(@PathVariable("id") Long id, Model model) {
        Team team = teamService.findById(id);
        List<Player> players = playerService.findByTeamId(id);
        
        model.addAttribute("team", team);
        model.addAttribute("players", players);
        return "team-details";
    }

    @GetMapping("/admin/team/new")
    public String showNewTeamForm(Model model) {
        model.addAttribute("team", new Team());
        model.addAttribute("tournaments", tournamentService.findAll());
        return "admin/form-team";
    }

    @PostMapping("/admin/team/new")
    public String saveTeam(@Valid @ModelAttribute("team") Team team, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("tournaments", tournamentService.findAll());
            return "admin/form-team";
        }
        teamService.save(team);
        return "redirect:/teams";
    }

    // 1. Mostrar formulario de edición
    @GetMapping("/admin/team/edit/{id}")
    public String showEditTeamForm(@PathVariable("id") Long id, Model model) {
        Team team = teamService.findById(id);
        if (team != null) {
            model.addAttribute("team", team);
            model.addAttribute("tournaments", tournamentService.findAll());
            return "admin/form-team";
        }
        return "redirect:/teams";
    }

    // 2. Procesar la edición
    @PostMapping("/admin/team/edit/{id}")
    public String updateTeam(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("team") Team team,
                             BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("tournaments", tournamentService.findAll());
            return "admin/form-team";
        }
        team.setId(id);
        teamService.save(team);
        return "redirect:/teams";
    }

    // 3. Borrar equipo
    @GetMapping("/admin/team/delete/{id}")
    public String deleteTeam(@PathVariable("id") Long id) {
        teamService.deleteById(id);
        return "redirect:/teams";
    }
}