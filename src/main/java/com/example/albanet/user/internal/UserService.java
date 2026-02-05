package com.example.albanet.user.internal;

import com.example.albanet.user.api.UserApi;
import com.example.albanet.user.api.dto.UserDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class UserService implements UserApi {

    private final UserRepository repository;
    private final UserMapper mapper;

    public UserService(UserRepository repository, UserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<UserDto> findById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    public Optional<UserDto> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDto);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public UserDto createUser(String firstName, String lastName, String email, String encodedPassword,
                              String phoneNumber, String street, String city, String postalCode, String country) {
        if (repository.existsByEmail(email)) {
            throw new IllegalStateException("Email already in use");
        }

        UserEntity user = new UserEntity();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setPhoneNumber(phoneNumber);
        user.setStreet(street);
        user.setCity(city);
        user.setPostalCode(postalCode);
        user.setCountry(country);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        UserEntity saved = repository.save(user);
        return mapper.toDto(saved);
    }

    // ========== Internal methods (for use within user module only) ==========

    public UserEntity getEntityByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    public Optional<UserEntity> findEntityByEmail(String email) {
        return repository.findByEmail(email);
    }

    public Optional<Long> getUserIdByEmail(String email) {
        return repository.findByEmail(email).map(UserEntity::getId);
    }
}
