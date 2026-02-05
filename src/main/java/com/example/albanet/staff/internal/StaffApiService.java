package com.example.albanet.staff.internal;

import com.example.albanet.staff.api.StaffApi;
import com.example.albanet.staff.api.dto.CreateStaffRequest;
import com.example.albanet.staff.api.dto.StaffDto;
import com.example.albanet.staff.internal.enums.StaffRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of StaffApi for cross-module access.
 */
@Service
public class StaffApiService implements StaffApi {

    private final StaffRepository repository;
    private final StaffService staffService;

    public StaffApiService(StaffRepository repository, StaffService staffService) {
        this.repository = repository;
        this.staffService = staffService;
    }

    @Override
    public Optional<StaffDto> findByEmail(String email) {
        return repository.findByEmail(email).map(this::toDto);
    }

    @Override
    public StaffDto getActiveStaffByEmail(String email) {
        return toDto(staffService.getActiveStaffByEmail(email));
    }

    @Override
    public String getStaffFullNameByEmail(String email) {
        StaffEntity staff = staffService.getActiveStaffByEmail(email);
        return staff.getFirstName() + " " + staff.getLastName();
    }

    @Override
    public List<StaffDto> getActiveStaffByRole(String role) {
        StaffRole staffRole = StaffRole.valueOf(role);
        return staffService.getActiveStaffByRole(staffRole)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
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
    @Transactional
    public StaffDto createStaff(CreateStaffRequest request) {
        return toDto(staffService.createStaff(request));
    }

    @Override
    @Transactional
    public StaffDto createAdmin(String firstName, String lastName, String email, String encodedPassword,
                                String phoneNumber, String employeeNumber) {
        if (repository.existsByEmail(email)) {
            throw new IllegalStateException("Email already in use");
        }

        StaffEntity admin = new StaffEntity();
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setEmail(email);
        admin.setPassword(encodedPassword);
        admin.setPhoneNumber(phoneNumber);
        admin.setEmployeeNumber(employeeNumber);
        admin.setRole(StaffRole.ADMIN);
        admin.setHiredAt(LocalDateTime.now());
        admin.setActive(true);

        return toDto(repository.save(admin));
    }

    private StaffDto toDto(StaffEntity entity) {
        StaffDto dto = new StaffDto();
        dto.setId(entity.getId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setEmail(entity.getEmail());
        dto.setEmployeeNumber(entity.getEmployeeNumber());
        dto.setRole(entity.getRole().name());
        dto.setActive(entity.isActive());
        return dto;
    }
}
