package com.example.albanet.subscription.internal;

import com.example.albanet.subscription.api.dto.SubscriptionDto;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {

    public SubscriptionDto toDto(SubscriptionEntity entity) {
        if (entity == null) {
            return null;
        }

        SubscriptionDto dto = new SubscriptionDto();
        dto.setId(entity.getId());
        dto.setCatalogId(entity.getCatalogId());
        dto.setMonthsPurchased(entity.getMonthsPurchased());
        dto.setPriceAtStart(entity.getPriceAtStart());
        dto.setStartAt(entity.getStartAt());
        dto.setEndAt(entity.getEndAt());
        dto.setActive(entity.isActive());

        return dto;
    }
}
