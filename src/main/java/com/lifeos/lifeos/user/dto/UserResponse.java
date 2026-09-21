package com.lifeos.lifeos.user.dto;

import com.lifeos.lifeos.user.entity.Role;
import com.lifeos.lifeos.user.entity.User;

import java.util.UUID;

public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private Role role;

    public UserResponse(
            UUID id,
            String name,
            String email,
            Role role) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }
}
