package com.example.albanet.catalog.internal;

import com.example.albanet.catalog.api.dto.CatalogDto;
import org.springframework.stereotype.Component;

@Component
public class CatalogMapper {

    public CatalogDto toDto(CatalogEntity entity) {
        if (entity == null) {
            return null;
        }

        CatalogDto dto = new CatalogDto();
        dto.setId(entity.getId());
        dto.setProductType(entity.getProductType());
        dto.setCode(entity.getCode());
        dto.setDisplayName(entity.getDisplayName());
        dto.setPricePerMonth(entity.getPricePerMonth());
        dto.setActive(entity.isActive());

        return dto;
    }
}
