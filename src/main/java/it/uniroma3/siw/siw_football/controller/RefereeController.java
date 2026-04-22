package it.uniroma3.siw.siw_football.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import it.uniroma3.siw.siw_football.model.Referee;
import it.uniroma3.siw.siw_football.repository.RefereeRepository;
import it.uniroma3.siw.siw_football.service.RefereeService;

@Controller
public class RefereeController {
    @Autowired private RefereeService refereeService;
    @Autowired private RefereeRepository refereeRepository;

    @GetMapping("/admin/referee/new")
    public String showNewRefereeForm(Model model) {
        model.addAttribute("referee", new Referee());
        return "admin/form-referee";
    }

    @PostMapping("/admin/referee/save")
    public String saveReferee(@ModelAttribute("referee") Referee referee, BindingResult result, Model model) {
        
        // 1. Comprobamos si el código ya existe (solo si es un árbitro nuevo o ha cambiado el código)
        if (refereeRepository.existsByRefereeCode(referee.getRefereeCode())) {
            // 2. Añadimos el error al campo 'refereeCode'
            result.rejectValue("refereeCode", "error.referee", "Este código arbitral ya está asignado a otro árbitro.");
        }

        // 3. Si hay errores (por el código único o por validaciones normales), volvemos al formulario
        if (result.hasErrors()) {
            return "admin/form-referee";
        }

        refereeService.saveReferee(referee);
        return "redirect:/referees";
    }

    @GetMapping("/referees")
    public String listReferees(Model model) {
        // Usamos el Service para traer todos los árbitros
        model.addAttribute("referees", refereeService.findAll());
        return "referees"; // Nombre del archivo html que te pasé antes
    }

    @GetMapping("/admin/referee/edit/{id}")
    public String showEditRefereeForm(@PathVariable("id") Long id, Model model) {
        Referee referee = refereeService.findById(id);
        if (referee != null) {
            model.addAttribute("referee", referee);
            return "admin/form-referee";
        }
        return "redirect:/referees";
    }

    @GetMapping("/admin/referee/delete/{id}")
    public String deleteReferee(@PathVariable("id") Long id) {
        refereeService.deleteById(id);
        return "redirect:/referees";
    }
}
