package it.uniroma3.siw.siw_football.controller;

import it.uniroma3.siw.siw_football.model.Team;
import it.uniroma3.siw.siw_football.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class TeamController {

    @Autowired
    private TeamService teamService;

    @GetMapping("/team/{id}")
    public String showTeamDetails(@PathVariable("id") Long id, Model model) {
        Team team = teamService.findById(id);
        
        if (team != null) {
            model.addAttribute("team", team);
            return "team-details"; // Nos llevará al nuevo HTML
        }
        
        return "redirect:/"; // Si alguien pone un ID falso a mano, lo mandamos a la portada
    }
}