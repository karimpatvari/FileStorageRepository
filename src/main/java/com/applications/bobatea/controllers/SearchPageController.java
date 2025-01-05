package com.applications.bobatea.controllers;

import com.applications.bobatea.dto.FileDto;
import com.applications.bobatea.dto.SearchPageDto;
import com.applications.bobatea.dto.UserDto;
import com.applications.bobatea.models.User;
import com.applications.bobatea.services.MinIoService;
import com.applications.bobatea.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SearchPageController {

    private static final Logger logger = LoggerFactory.getLogger(SearchPageController.class);
    private MinIoService minIoService;
    private UserService userService;

    @Autowired
    public SearchPageController(MinIoService minIoService, UserService userService) {
        this.minIoService = minIoService;
        this.userService = userService;
    }

    @GetMapping("/search")
    public String searchForFiles(@RequestParam(value = "keyword" ,required = false) String keyword,
                                 Model model, Principal principal) {
        try {
            validatePrincipal(principal);
            User user = userService.getAuthenticatedUser(principal);
            UserDto userDto = new UserDto(user.getId(), user.getUsername());

            if (keyword == null || keyword.isEmpty()) {

                SearchPageDto searchPageDto = new SearchPageDto(userDto, null ,"");

                model.addAttribute(searchPageDto);
                return "search-page";
            }

            List<FileDto> files = minIoService.findFiles(user, keyword);

            SearchPageDto searchPageDto = new SearchPageDto(userDto, (ArrayList<FileDto>) files,keyword);

            model.addAttribute(searchPageDto);
            return "search-page";

        }  catch (Exception e) {
            logger.error("Error while searching files: {}", e.getMessage(), e);

            model.addAttribute("message", e.getMessage());
            return "error-page";
        }
    }

    private void validatePrincipal(Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("User not found");
        }
    }
}
