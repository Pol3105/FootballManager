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
        return "redirect:/login?disabled";
    }

    @PostMapping("/register")
    public String registerUser() {
        return "redirect:/login?disabled";
    }
}
