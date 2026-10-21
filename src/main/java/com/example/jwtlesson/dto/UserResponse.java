package com.example.jwtlesson.dto;

import com.example.jwtlesson.entity.User;

import java.util.List;

public record UserResponse(
        Long id,
        String username,
        boolean enabled,
        List<String> roles) {

    public static UserResponse from(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .sorted()
                .toList();
        return new UserResponse(user.getId(), user.getUsername(), user.isEnabled(), roleNames);
    }
}
