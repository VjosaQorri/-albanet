package com.example.albanet.subscription.internal;

import com.example.albanet.subscription.api.SubscriptionApi;
import com.example.albanet.subscription.api.dto.SubscriptionPlanDto;
import com.example.albanet.subscription.api.dto.UserSubscriptionDto;
import com.example.albanet.subscription.internal.enums.CatalogCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionPlanService implements SubscriptionApi {

    private final SubscriptionPlanRepository planRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionMapper mapper;

    public SubscriptionPlanService(SubscriptionPlanRepository planRepository,
                                   UserSubscriptionRepository userSubscriptionRepository,
                                   SubscriptionMapper mapper) {
        this.planRepository = planRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.mapper = mapper;
    }

    // ========== API Methods (for other modules) ==========

    @Override
    public List<SubscriptionPlanDto> getPlansByCategory(String category) {
        return planRepository.findByCategoryAndActiveTrue(category)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserSubscriptionDto getActiveSubscriptionByCategory(Long userId, String category) {
        List<UserSubscriptionEntity> activeSubscriptions =
            userSubscriptionRepository.findByUserIdAndCategoryAndStatus(
                userId, category, UserSubscriptionEntity.SubscriptionStatus.ACTIVE);
        return activeSubscriptions.isEmpty() ? null : mapper.toDto(activeSubscriptions.get(0));
    }

    @Override
    public UserSubscriptionDto getPendingSubscriptionByCategory(Long userId, String category) {
        List<UserSubscriptionEntity> pendingSubscriptions =
            userSubscriptionRepository.findByUserIdAndCategoryAndStatus(
                userId, category, UserSubscriptionEntity.SubscriptionStatus.PENDING);
        return pendingSubscriptions.isEmpty() ? null : mapper.toDto(pendingSubscriptions.get(0));
    }

    @Override
    @Transactional
    public UserSubscriptionDto subscribe(Long userId, String planCode, Integer durationMonths) {
        CatalogCode code = CatalogCode.valueOf(planCode);
        return mapper.toDto(subscribeInternal(userId, code, durationMonths));
    }

    @Override
    public List<UserSubscriptionDto> getUserSubscriptions(Long userId) {
        return userSubscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelSubscription(Long subscriptionId, Long userId) {
        UserSubscriptionEntity subscription = userSubscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        if (!subscription.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: This subscription does not belong to you");
        }

        if (subscription.getStatus() != UserSubscriptionEntity.SubscriptionStatus.PENDING) {
            throw new RuntimeException("Only pending subscriptions can be cancelled");
        }

        userSubscriptionRepository.delete(subscription);
    }

    // ========== Internal Methods (for use within subscription module) ==========

    /**
     * Get plan entity by code (internal use only)
     */
    public SubscriptionPlanEntity getPlanByCode(CatalogCode code) {
        return planRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new RuntimeException("Plan not found: " + code));
    }

    /**
     * Get active subscription entity by category (internal use only)
     */
    public UserSubscriptionEntity getActiveSubscriptionEntityByCategory(Long userId, String category) {
        List<UserSubscriptionEntity> activeSubscriptions =
            userSubscriptionRepository.findByUserIdAndCategoryAndStatus(
                userId, category, UserSubscriptionEntity.SubscriptionStatus.ACTIVE);
        return activeSubscriptions.isEmpty() ? null : activeSubscriptions.get(0);
    }

    /**
     * Get pending subscription entity by category (internal use only)
     */
    public UserSubscriptionEntity getPendingSubscriptionEntityByCategory(Long userId, String category) {
        List<UserSubscriptionEntity> pendingSubscriptions =
            userSubscriptionRepository.findByUserIdAndCategoryAndStatus(
                userId, category, UserSubscriptionEntity.SubscriptionStatus.PENDING);
        return pendingSubscriptions.isEmpty() ? null : pendingSubscriptions.get(0);
    }

    /**
     * Subscribe user to a plan (internal implementation)
     */
    @Transactional
    public UserSubscriptionEntity subscribeInternal(Long userId, CatalogCode planCode, Integer durationMonths) {
        SubscriptionPlanEntity plan = getPlanByCode(planCode);

        UserSubscriptionEntity existingSubscription = getActiveSubscriptionEntityByCategory(userId, plan.getCategory());
        UserSubscriptionEntity pendingSubscription = getPendingSubscriptionEntityByCategory(userId, plan.getCategory());

        if (pendingSubscription != null && pendingSubscription.getPlanCode() == planCode) {
            return extendSubscription(pendingSubscription, plan, durationMonths);
        }

        if (pendingSubscription != null && pendingSubscription.getPlanCode() != planCode) {
            userSubscriptionRepository.delete(pendingSubscription);
            if (PlanHierarchy.isHierarchical(planCode)) {
                return createPendingSubscription(userId, existingSubscription, plan, durationMonths);
            } else {
                return extendSubscription(existingSubscription, plan, durationMonths);
            }
        }

        if (existingSubscription != null) {
            if (existingSubscription.getPlanCode() == planCode) {
                return extendSubscription(existingSubscription, plan, durationMonths);
            } else {
                if (PlanHierarchy.isHierarchical(planCode)) {
                    return createPendingSubscription(userId, existingSubscription, plan, durationMonths);
                } else {
                    return extendSubscription(existingSubscription, plan, durationMonths);
                }
            }
        }

        return createNewSubscription(userId, plan, durationMonths);
    }

    /**
     * Get plan entities by category (internal use only)
     */
    public List<SubscriptionPlanEntity> getPlanEntitiesByCategory(String category) {
        return planRepository.findByCategoryAndActiveTrue(category);
    }

    /**
     * Create a new subscription
     */
    private UserSubscriptionEntity createNewSubscription(Long userId, SubscriptionPlanEntity plan, Integer durationMonths) {
        UserSubscriptionEntity subscription = new UserSubscriptionEntity();
        subscription.setUserId(userId);
        subscription.setPlanId(plan.getId());
        subscription.setPlanCode(plan.getCode());
        subscription.setPlanName(plan.getName());
        subscription.setCategory(plan.getCategory());
        subscription.setDurationMonths(durationMonths);

        // Calculate dates and price
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate;
        BigDecimal totalPrice;

        if ("MOBILE".equals(plan.getCategory())) {
            // Mobile plans are in days (15 or 30)
            int totalDays = plan.getDurationDays();
            endDate = startDate.plusDays(totalDays);
            totalPrice = plan.getMonthlyPrice(); // For mobile, price is per cycle
        } else {
            // TV and Internet are in months
            endDate = startDate.plusMonths(durationMonths);
            totalPrice = plan.getMonthlyPrice().multiply(BigDecimal.valueOf(durationMonths));
        }

        subscription.setStartDate(startDate);
        subscription.setEndDate(endDate);
        subscription.setTotalPrice(totalPrice);
        subscription.setStatus(UserSubscriptionEntity.SubscriptionStatus.ACTIVE);

        return userSubscriptionRepository.save(subscription);
    }

    /**
     * Create a pending subscription that starts after current one ends
     */
    private UserSubscriptionEntity createPendingSubscription(Long userId, UserSubscriptionEntity currentSub,
                                                             SubscriptionPlanEntity newPlan, Integer durationMonths) {
        UserSubscriptionEntity pendingSubscription = new UserSubscriptionEntity();
        pendingSubscription.setUserId(userId);
        pendingSubscription.setPlanId(newPlan.getId());
        pendingSubscription.setPlanCode(newPlan.getCode());
        pendingSubscription.setPlanName(newPlan.getName());
        pendingSubscription.setCategory(newPlan.getCategory());
        pendingSubscription.setDurationMonths(durationMonths);

        // Start from current subscription's end date
        LocalDateTime startDate = currentSub.getEndDate();
        LocalDateTime endDate = startDate.plusMonths(durationMonths);
        BigDecimal totalPrice = newPlan.getMonthlyPrice().multiply(BigDecimal.valueOf(durationMonths));

        pendingSubscription.setStartDate(startDate);
        pendingSubscription.setEndDate(endDate);
        pendingSubscription.setTotalPrice(totalPrice);
        pendingSubscription.setStatus(UserSubscriptionEntity.SubscriptionStatus.PENDING);

        return userSubscriptionRepository.save(pendingSubscription);
    }

    /**
     * Extend existing subscription
     */
    @Transactional
    public UserSubscriptionEntity extendSubscription(UserSubscriptionEntity existingSubscription,
                                                     SubscriptionPlanEntity plan,
                                                     Integer durationMonths) {
        // Calculate new end date from current end date
        LocalDateTime newEndDate;
        BigDecimal additionalPrice;

        if ("MOBILE".equals(plan.getCategory())) {
            // Mobile plans are in days
            int totalDays = plan.getDurationDays();
            newEndDate = existingSubscription.getEndDate().plusDays(totalDays);
            additionalPrice = plan.getMonthlyPrice();
        } else {
            // TV and Internet are in months
            newEndDate = existingSubscription.getEndDate().plusMonths(durationMonths);
            additionalPrice = plan.getMonthlyPrice().multiply(BigDecimal.valueOf(durationMonths));
        }

        // Update existing subscription
        existingSubscription.setEndDate(newEndDate);
        existingSubscription.setDurationMonths(existingSubscription.getDurationMonths() + durationMonths);
        existingSubscription.setTotalPrice(existingSubscription.getTotalPrice().add(additionalPrice));

        return userSubscriptionRepository.save(existingSubscription);
    }

    /**
     * Get user's active subscriptions (internal use)
     */
    public List<UserSubscriptionEntity> getUserActiveSubscriptions(Long userId) {
        return userSubscriptionRepository.findByUserIdAndStatus(
                userId, UserSubscriptionEntity.SubscriptionStatus.ACTIVE);
    }
}
