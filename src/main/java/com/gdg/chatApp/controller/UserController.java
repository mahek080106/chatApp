package com.gdg.chatApp.controller;

import com.gdg.chatApp.dto.UserResponse;
import com.gdg.chatApp.model.User;
import com.gdg.chatApp.service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // authentication.getName() is the email we put in the JWT
    @GetMapping
    public List<UserResponse> getUsers(Authentication authentication) {
        User me = userService.getByEmail(authentication.getName());
        return userService.getAllExcept(me);
    }
}
