package com.meetingnotes.dto.auth;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String fullName,
        String email
) {
    public static AuthResponse of(String token, Long userId, String fullName, String email) {
        return new AuthResponse(token, "Bearer", userId, fullName, email);
    }
}
