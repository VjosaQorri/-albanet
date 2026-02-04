package com.example.albanet.staff.internal;

import com.example.albanet.staff.internal.enums.StaffRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<StaffEntity, Long> {

    Optional<StaffEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmployeeNumber(String employeeNumber);

    List<StaffEntity> findByRoleAndActiveTrue(StaffRole role);

    List<StaffEntity> findByActiveTrue();
}
