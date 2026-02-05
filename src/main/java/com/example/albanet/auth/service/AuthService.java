package com.example.albanet.auth.service;

import com.example.albanet.auth.dto.LoginRequest;
import com.example.albanet.auth.dto.RegisterRequest;
import com.example.albanet.user.api.UserApi;
import com.example.albanet.user.api.dto.UserDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserApi userApi;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserApi userApi, PasswordEncoder passwordEncoder) {
        this.userApi = userApi;
        this.passwordEncoder = passwordEncoder;
    }

    public String login(LoginRequest request) {
        // placeholder: in a real app validate credentials and return a JWT or session id
        return "dummy-token";
    }

    public Long register(RegisterRequest request) {
        // placeholder: persist a new user and return its id
        return 1L;
    }

    public UserDto registerUser(RegisterRequest request) {
        // Check if user already exists
        if (userApi.existsByEmail(request.getEmail())) {
            throw new RuntimeException("An account with this email already exists");
        }

        // Create new user via API
        return userApi.createUser(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhoneNumber(),
                request.getStreet(),
                request.getCity(),
                request.getPostalCode(),
                request.getCountry()
        );
    }
}
