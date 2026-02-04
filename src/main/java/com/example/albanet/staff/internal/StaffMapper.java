package com.example.albanet.staff.internal;

import com.example.albanet.staff.api.dto.StaffDto;
import org.springframework.stereotype.Component;

@Component
public class StaffMapper {

    public StaffDto toDto(StaffEntity entity) {
        if (entity == null) {
            return null;
        }

        StaffDto dto = new StaffDto();
        dto.setId(entity.getId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setEmail(entity.getEmail());
        dto.setEmployeeNumber(entity.getEmployeeNumber());
        dto.setRole(entity.getRole() != null ? entity.getRole().name() : null);
        dto.setActive(entity.isActive());

        return dto;
    }
}
