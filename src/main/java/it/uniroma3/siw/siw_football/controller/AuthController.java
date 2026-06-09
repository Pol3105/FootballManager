package it.uniroma3.siw.siw_football.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import it.uniroma3.siw.siw_football.model.User;
import it.uniroma3.siw.siw_football.service.UserService;
import jakarta.validation.Valid;
import org.springframework.ui.Model;

@Controller
public class AuthController {

    @Autowired private UserService userService;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, 
                               BindingResult result,
                               @RequestParam(value = "securityAnswer", required = false) String securityAnswer,
                               Model model) {
        
        // Reto matemático anti-bot
        if (securityAnswer == null || !"8".equals(securityAnswer.trim())) {
            model.addAttribute("securityError", "Respuesta incorrecta. Por favor, resuelve la suma para demostrar que eres humano.");
            return "register";
        }

        if (result.hasErrors()) {
            return "register";
        }
        user.setRole("USER");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.saveUser(user);
        return "redirect:/login";
    }
}
