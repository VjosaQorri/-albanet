package com.example.albanet.staff.internal;

import com.example.albanet.staff.api.dto.CreateStaffRequest;
import com.example.albanet.staff.internal.enums.StaffRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StaffServiceImpl implements StaffService {

    private final StaffRepository repository;
    private final PasswordEncoder passwordEncoder;

    public StaffServiceImpl(StaffRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<StaffEntity> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public StaffEntity getActiveStaffByEmail(String email) {
        Optional<StaffEntity> staffOpt = repository.findByEmail(email);

        if (staffOpt.isEmpty()) {
            throw new IllegalStateException("Staff not found");
        }

        StaffEntity staff = staffOpt.get();

        if (!staff.isActive()) {
            throw new IllegalStateException("Staff account is inactive");
        }

        return staff;
    }

    @Override
    public boolean isActive(Long staffId) {
        return repository.findById(staffId)
                .map(StaffEntity::isActive)
                .orElse(false);
    }

    @Override
    @Transactional
    public StaffEntity createStaff(CreateStaffRequest request) {
        // Check if email already exists
        if (repository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        // Check if employee number already exists
        if (repository.existsByEmployeeNumber(request.getEmployeeNumber())) {
            throw new IllegalArgumentException("Employee number already in use");
        }

        // Create new staff entity
        StaffEntity staff = new StaffEntity();
        staff.setFirstName(request.getFirstName());
        staff.setLastName(request.getLastName());
        staff.setEmail(request.getEmail());
        staff.setPassword(passwordEncoder.encode(request.getPassword()));
        staff.setPhoneNumber(request.getPhoneNumber());
        staff.setEmployeeNumber(request.getEmployeeNumber());
        staff.setRole(StaffRole.valueOf(request.getRole()));
        staff.setHiredAt(LocalDateTime.now());
        staff.setActive(true);

        return repository.save(staff);
    }

    @Override
    public List<StaffEntity> getActiveStaffByRole(StaffRole role) {
        return repository.findByRoleAndActiveTrue(role);
    }

    @Override
    public List<StaffEntity> getAllActiveStaff() {
        return repository.findByActiveTrue();
    }
}
