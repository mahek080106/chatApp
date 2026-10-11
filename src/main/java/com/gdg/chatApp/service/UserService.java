package com.gdg.chatApp.service;

import com.gdg.chatApp.dto.UserResponse;
import com.gdg.chatApp.model.User;
import com.gdg.chatApp.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Find the logged-in user from the email stored in the JWT
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "User no longer exists"));
    }

    // All users except the one who is asking
    public List<UserResponse> getAllExcept(User me) {
        return userRepository.findAll().stream()
            .filter(user -> !user.getId().equals(me.getId()))
            .map(UserResponse::from)
            .toList();
    }
}
