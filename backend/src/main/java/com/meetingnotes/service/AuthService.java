package com.meetingnotes.service;

import com.meetingnotes.dto.auth.AuthResponse;
import com.meetingnotes.dto.auth.LoginRequest;
import com.meetingnotes.dto.auth.RegisterRequest;
import com.meetingnotes.entity.Role;
import com.meetingnotes.entity.User;
import com.meetingnotes.exception.BadRequestException;
import com.meetingnotes.repository.UserRepository;
import com.meetingnotes.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("An account with that email already exists");
        }

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return AuthResponse.of(token, user.getId(), user.getFullName(), user.getEmail());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Direct credential check: look up the user, then compare the password
        // against the stored BCrypt hash. Returns a clean 400 on any mismatch.
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);
        return AuthResponse.of(token, user.getId(), user.getFullName(), user.getEmail());
    }
}
