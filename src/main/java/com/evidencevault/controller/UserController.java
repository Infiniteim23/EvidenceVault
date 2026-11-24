package com.evidencevault.controller;

import com.evidencevault.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    private final UserRepository users;

    public UserController(UserRepository users) {
        this.users = users;
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", users.findAll());
        return "users/list";
    }
}
