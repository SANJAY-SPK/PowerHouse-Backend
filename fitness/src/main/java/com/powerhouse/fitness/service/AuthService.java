package com.powerhouse.fitness.service;

import com.powerhouse.fitness.dto.request.LoginRequest;
import com.powerhouse.fitness.dto.request.RegisterRequest;
import com.powerhouse.fitness.dto.response.AuthResponse;
import com.powerhouse.fitness.entity.Role;
import com.powerhouse.fitness.entity.User;
import com.powerhouse.fitness.repository.UserRepository;
import com.powerhouse.fitness.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .email(user.getEmail())
                .build();
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        Role role = request.getRole() != null
                ? Role.valueOf(request.getRole().toUpperCase())
                : Role.MEMBER;

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();
        userRepository.save(user);
        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .email(user.getEmail())
                .build();
    }
}