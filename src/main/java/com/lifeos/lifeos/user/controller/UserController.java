package com.lifeos.lifeos.user.controller;

import com.lifeos.lifeos.user.dto.UserResponse;
import com.lifeos.lifeos.user.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping("/me")
    public UserResponse getCurrentUser(
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        return UserResponse.from(user);
    }
}