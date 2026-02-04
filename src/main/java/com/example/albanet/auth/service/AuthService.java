package com.example.albanet.auth.service;

import com.example.albanet.auth.dto.LoginRequest;
import com.example.albanet.auth.dto.RegisterRequest;
import com.example.albanet.user.internal.UserEntity;
import com.example.albanet.user.internal.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String login(LoginRequest request) {
        // placeholder: in a real app validate credentials and return a JWT or session id
        return "dummy-token";
    }

    public Long register(RegisterRequest request) {
        // placeholder: persist a new user and return its id
        return 1L;
    }

    public UserEntity registerUser(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("An account with this email already exists");
        }

        // Create new user entity
        UserEntity user = new UserEntity();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setStreet(request.getStreet());
        user.setCity(request.getCity());
        user.setPostalCode(request.getPostalCode());
        user.setCountry(request.getCountry());
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        // Save and return the user
        return userRepository.save(user);
    }
}
