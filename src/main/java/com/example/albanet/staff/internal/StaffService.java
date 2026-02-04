package com.example.albanet.staff.internal;

import com.example.albanet.staff.api.dto.CreateStaffRequest;
import com.example.albanet.staff.internal.enums.StaffRole;

import java.util.List;
import java.util.Optional;

public interface StaffService {

    Optional<StaffEntity> findByEmail(String email);

    StaffEntity getActiveStaffByEmail(String email);

    boolean isActive(Long staffId);

    StaffEntity createStaff(CreateStaffRequest request);

    List<StaffEntity> getActiveStaffByRole(StaffRole role);

    List<StaffEntity> getAllActiveStaff();
}
