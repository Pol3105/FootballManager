package it.uniroma3.siw.siw_football.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import it.uniroma3.siw.siw_football.model.Player;
import it.uniroma3.siw.siw_football.service.PlayerService;
import it.uniroma3.siw.siw_football.service.TeamService;

@Controller
public class PlayerController {
    @Autowired private PlayerService playerService;
    @Autowired private TeamService teamService;

    @GetMapping("/admin/player/new")
    public String showNewPlayerForm(Model model) {
        model.addAttribute("player", new Player());
        model.addAttribute("teams", teamService.findAll()); // Lista de equipos para el select
        return "admin/form-player";
    }

    @PostMapping("/admin/player/save")
    public String savePlayer(@ModelAttribute("player") Player player) {
        playerService.savePlayer(player);
        return "redirect:/players"; // O a la lista de jugadores
    }
    
    @GetMapping("/players")
    public String listPlayers(Model model) {
        // Usamos el Service para traer todos los jugadores
        model.addAttribute("players", playerService.findAll());
        return "players"; // Nombre del archivo html que te pasé antes
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
