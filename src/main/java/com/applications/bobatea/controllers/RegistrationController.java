package com.applications.bobatea.controllers;

import com.applications.bobatea.customExceptions.MinioClientException;
import com.applications.bobatea.customExceptions.UserExistsException;
import com.applications.bobatea.models.User;
import com.applications.bobatea.services.MinIoService;
import com.applications.bobatea.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
public class RegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);
    private UserService userService;
    private MinIoService minIoService;

    @Autowired
    public RegistrationController(UserService userService, MinIoService minIoService) {
        this.userService = userService;
        this.minIoService = minIoService;
    }

    @GetMapping("register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "registration-page";
    }

    @PostMapping("register")
    public String processRegistrationForm(@ModelAttribute("user") User user, Model model) {

        try {
            User newUser = userService.register(user);

            minIoService.createFolder(newUser);

        } catch (UserExistsException e) {
            model.addAttribute("user", new User());
            model.addAttribute("error", "User already exists");
            return "registration-page";

        } catch (MinioClientException e) {
            logger.error("Error with minio when registrating user and creating folder: {}", e.getMessage(), e);
            model.addAttribute("message", e.getMessage());
            return "error-page";
        } catch (Exception e) {
            logger.error("Unexpected error when registrating user: {}", e.getMessage(), e);
            model.addAttribute("message", e.getMessage());
            return "error-page";
        }

        return "redirect:/login";

    }

}
