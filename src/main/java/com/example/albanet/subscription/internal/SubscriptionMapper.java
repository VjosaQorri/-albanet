package com.example.albanet.subscription.internal;

import com.example.albanet.subscription.api.dto.SubscriptionPlanDto;
import com.example.albanet.subscription.api.dto.UserSubscriptionDto;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {

    public SubscriptionPlanDto toDto(SubscriptionPlanEntity entity) {
        if (entity == null) return null;

        SubscriptionPlanDto dto = new SubscriptionPlanDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode().name());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setCategory(entity.getCategory());
        dto.setMonthlyPrice(entity.getMonthlyPrice());
        dto.setDurationDays(entity.getDurationDays());
        dto.setFeatures(entity.getFeatures());
        dto.setActive(entity.getActive());
        return dto;
    }

    public UserSubscriptionDto toDto(UserSubscriptionEntity entity) {
        if (entity == null) return null;

        UserSubscriptionDto dto = new UserSubscriptionDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setPlanId(entity.getPlanId());
        dto.setPlanCode(entity.getPlanCode().name());
        dto.setPlanName(entity.getPlanName());
        dto.setCategory(entity.getCategory());
        dto.setDurationMonths(entity.getDurationMonths());
        dto.setTotalPrice(entity.getTotalPrice());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setStatus(entity.getStatus().name());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCancelledAt(entity.getCancelledAt());
        return dto;
    }
}
