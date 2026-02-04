package com.example.albanet.user.internal;

import com.example.albanet.user.api.dto.UserDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    public UserService(UserRepository repository, UserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public UserDto getById(Long id) {
        UserEntity user = repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        return mapper.toDto(user);
    }

    public UserEntity getByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    public UserDto register(UserEntity user) {
        if (repository.existsByEmail(user.getEmail())) {
            throw new IllegalStateException("Email already in use");
        }

        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        UserEntity saved = repository.save(user);
        return mapper.toDto(saved);
    }

    // authentication logic will go here next
}
