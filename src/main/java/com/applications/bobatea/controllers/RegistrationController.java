package com.applications.bobatea.controllers;

import com.applications.bobatea.exceptions.UserExistsException;
import com.applications.bobatea.models.User;
import com.applications.bobatea.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
public class RegistrationController {

    private UserService userService;

    @Autowired
    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "registration-page";
    }

    @PostMapping("register")
    public String processRegistrationForm(@ModelAttribute("user") User user, Model model, Principal principal) {

        try {
            userService.register(user);
        } catch (UserExistsException e) {
            model.addAttribute("user", new User());
            model.addAttribute("error", "User already exists");
            return "registration-page";
        }

        return "redirect:/login";

    }

}
