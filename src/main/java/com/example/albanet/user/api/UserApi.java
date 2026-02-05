package com.example.albanet.user.api;

import com.example.albanet.user.api.dto.UserDto;

import java.util.Optional;

/**
 * Public API for the User module.
 * Other modules should use this interface instead of accessing internal classes directly.
 */
public interface UserApi {

    /**
     * Find a user by their ID
     */
    Optional<UserDto> findById(Long id);

    /**
     * Find a user by their email
     */
    Optional<UserDto> findByEmail(String email);

    /**
     * Check if a user exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Get user count
     */
    long count();

    /**
     * Create a new user (for registration)
     */
    UserDto createUser(String firstName, String lastName, String email, String encodedPassword,
                       String phoneNumber, String street, String city, String postalCode, String country);
}
