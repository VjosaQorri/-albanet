package com.example.albanet.staff.api;

import com.example.albanet.staff.api.dto.CreateStaffRequest;
import com.example.albanet.staff.api.dto.StaffDto;

import java.util.List;
import java.util.Optional;

/**
 * Public API for the Staff module.
 * Other modules should use this interface instead of accessing internal classes directly.
 */
public interface StaffApi {

    /**
     * Get staff member by email
     */
    Optional<StaffDto> findByEmail(String email);

    /**
     * Get active staff by email (throws exception if not found)
     */
    StaffDto getActiveStaffByEmail(String email);

    /**
     * Get staff full name by email
     */
    String getStaffFullNameByEmail(String email);

    /**
     * Get active staff members by role
     */
    List<StaffDto> getActiveStaffByRole(String role);

    /**
     * Check if staff exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Get staff count
     */
    long count();

    /**
     * Create a new staff member
     */
    StaffDto createStaff(CreateStaffRequest request);

    /**
     * Create admin user (for initialization)
     */
    StaffDto createAdmin(String firstName, String lastName, String email, String encodedPassword,
                         String phoneNumber, String employeeNumber);
}
