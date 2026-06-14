package it.uniroma3.siw.siw_football.controller;

import it.uniroma3.siw.siw_football.service.RefereeService;
import it.uniroma3.siw.siw_football.service.TeamService;
import it.uniroma3.siw.siw_football.service.TournamentService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import it.uniroma3.siw.siw_football.model.Match;

import it.uniroma3.siw.siw_football.model.Team;
import it.uniroma3.siw.siw_football.model.Tournament; 

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private RefereeService refereeService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("tournaments", tournamentService.findAll());
        return "index";
    }


    @GetMapping("/tournament/{id}")
    public String showTournamentDetails(@PathVariable("id") Long id,
                                        @RequestParam(defaultValue = "0") int page,
                                        Model model) {
        Tournament tournament = tournamentService.findById(id);
        if (tournament == null) return "redirect:/";

        Page<Match> matchPage = tournamentService.findMatchesPaginated(id, page, 5);
        model.addAttribute("tournament", tournament);
        model.addAttribute("matchPage", matchPage);
        model.addAttribute("referees", refereeService.findAll());
        // Para las flechas de paginación del dock
        model.addAttribute("currentPage", matchPage.getNumber());
        model.addAttribute("totalPages", matchPage.getTotalPages());
        return "tournament-details";
    }

    // 1. Mostrar el formulario (GET)
    @GetMapping("/admin/tournament/new")
    public String showNewTournamentForm(Model model) {
        // Le pasamos a Thymeleaf un torneo VACÍO para que lo rellene
        model.addAttribute("tournament", new Tournament());
        return "admin/form-tournament"; 
    }
    
    // 2. Recibir los datos y guardarlos (POST)
    @PostMapping("/admin/tournament/new")
    public String saveNewTournament(@Valid @ModelAttribute("tournament") Tournament tournament,
                                    BindingResult result) {
        if (result.hasErrors()) return "admin/form-tournament";
        tournamentService.save(tournament);
        return "redirect:/";
    }


    // 1. Mostrar el formulario con los datos ya rellenos (GET)
    @GetMapping("/admin/tournament/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Tournament tournament = tournamentService.findById(id);
        if (tournament != null) {
            model.addAttribute("tournament", tournament);
            return "admin/form-tournament"; // Reutilizamos el mismo formulario
        }
        return "redirect:/";
    }

    // 2. Procesar los cambios realizados (POST)
    @PostMapping("/admin/tournament/edit/{id}")
    public String updateTournament(@PathVariable("id") Long id,
                                   @Valid @ModelAttribute("tournament") Tournament tournament,
                                   BindingResult result) {
        if (result.hasErrors()) return "admin/form-tournament";
        tournament.setId(id);
        tournamentService.save(tournament);
        return "redirect:/tournament/" + id;
    }

    // 3. Borrar el torneo (GET)
    @GetMapping("/admin/tournament/delete/{id}")
    public String deleteTournament(@PathVariable("id") Long id) {
        tournamentService.deleteById(id);
        return "redirect:/";
    }

    // 1. Mostrar la página de gestión
    @GetMapping("/admin/tournament/{id}/manage-teams")
    public String manageTournamentTeams(@PathVariable("id") Long id, Model model) {
        Tournament tournament = tournamentService.findById(id);
        List<Team> allTeams = teamService.findAll();
        
        model.addAttribute("tournament", tournament);
        model.addAttribute("allTeams", allTeams);
        return "admin/manage-tournament-teams";
    }

    // 2. Acción de añadir
    @GetMapping("/admin/tournament/{id}/add-team/{teamId}")
    public String addTeam(@PathVariable("id") Long id, @PathVariable("teamId") Long teamId) {
        tournamentService.addTeamToTournament(id, teamId);
        return "redirect:/admin/tournament/" + id + "/manage-teams";
    }

    // 3. Acción de quitar
    @GetMapping("/admin/tournament/{id}/remove-team/{teamId}")
    public String removeTeam(@PathVariable("id") Long id, @PathVariable("teamId") Long teamId) {
        tournamentService.removeTeamFromTournament(id, teamId);
        return "redirect:/admin/tournament/" + id + "/manage-teams";
    }
}