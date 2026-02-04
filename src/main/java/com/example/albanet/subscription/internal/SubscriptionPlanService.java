package com.example.albanet.subscription.internal;

import com.example.albanet.catalog.internal.enums.CatalogCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository planRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    public SubscriptionPlanService(SubscriptionPlanRepository planRepository,
                                   UserSubscriptionRepository userSubscriptionRepository) {
        this.planRepository = planRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
    }

    /**
     * Get all plans by category
     */
    public List<SubscriptionPlanEntity> getPlansByCategory(String category) {
        return planRepository.findByCategoryAndActiveTrue(category);
    }

    /**
     * Get plan by code
     */
    public SubscriptionPlanEntity getPlanByCode(CatalogCode code) {
        return planRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new RuntimeException("Plan not found: " + code));
    }

    /**
     * Check if user has active subscription for a category
     */
    public UserSubscriptionEntity getActiveSubscriptionByCategory(Long userId, String category) {
        List<UserSubscriptionEntity> activeSubscriptions =
            userSubscriptionRepository.findByUserIdAndCategoryAndStatus(
                userId, category, UserSubscriptionEntity.SubscriptionStatus.ACTIVE);
        return activeSubscriptions.isEmpty() ? null : activeSubscriptions.get(0);
    }

    /**
     * Check if user has pending subscription for a category
     */
    public UserSubscriptionEntity getPendingSubscriptionByCategory(Long userId, String category) {
        List<UserSubscriptionEntity> pendingSubscriptions =
            userSubscriptionRepository.findByUserIdAndCategoryAndStatus(
                userId, category, UserSubscriptionEntity.SubscriptionStatus.PENDING);
        return pendingSubscriptions.isEmpty() ? null : pendingSubscriptions.get(0);
    }

    /**
     * Subscribe user to a plan or extend/change existing subscription
     */
    @Transactional
    public UserSubscriptionEntity subscribe(Long userId, CatalogCode planCode, Integer durationMonths) {
        SubscriptionPlanEntity plan = getPlanByCode(planCode);

        // Check if user already has an active subscription for this category
        UserSubscriptionEntity existingSubscription = getActiveSubscriptionByCategory(userId, plan.getCategory());

        // Check if user has a pending subscription for this category
        UserSubscriptionEntity pendingSubscription = getPendingSubscriptionByCategory(userId, plan.getCategory());

        // Priority 1: If clicking on pending plan - extend it
        if (pendingSubscription != null && pendingSubscription.getPlanCode() == planCode) {
            return extendSubscription(pendingSubscription, plan, durationMonths);
        }

        // Priority 2: If there's a pending subscription but user clicks different plan - replace pending
        if (pendingSubscription != null && pendingSubscription.getPlanCode() != planCode) {
            // Delete old pending subscription and create new one
            userSubscriptionRepository.delete(pendingSubscription);
            if (PlanHierarchy.isHierarchical(planCode)) {
                return createPendingSubscription(userId, existingSubscription, plan, durationMonths);
            } else {
                return extendSubscription(existingSubscription, plan, durationMonths);
            }
        }

        // Priority 3: Handle active subscription
        if (existingSubscription != null) {
            // Check if it's the same plan
            if (existingSubscription.getPlanCode() == planCode) {
                // Same plan - extend it
                return extendSubscription(existingSubscription, plan, durationMonths);
            } else {
                // Different plan - check if upgrade/downgrade or just extend for mobile
                if (PlanHierarchy.isHierarchical(planCode)) {
                    // TV/Internet - create pending subscription for upgrade/downgrade
                    return createPendingSubscription(userId, existingSubscription, plan, durationMonths);
                } else {
                    // Mobile - just extend with new plan
                    return extendSubscription(existingSubscription, plan, durationMonths);
                }
            }
        }

        // Create new subscription
        return createNewSubscription(userId, plan, durationMonths);
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
     * Get user's active subscriptions
     */
    public List<UserSubscriptionEntity> getUserActiveSubscriptions(Long userId) {
        return userSubscriptionRepository.findByUserIdAndStatus(
                userId, UserSubscriptionEntity.SubscriptionStatus.ACTIVE);
    }

    /**
     * Get all user subscriptions
     */
    public List<UserSubscriptionEntity> getUserSubscriptions(Long userId) {
        return userSubscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Cancel a pending subscription
     */
    @Transactional
    public void cancelSubscription(Long subscriptionId, Long userId) {
        UserSubscriptionEntity subscription = userSubscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        // Verify the subscription belongs to this user
        if (!subscription.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: This subscription does not belong to you");
        }

        // Only allow canceling PENDING subscriptions
        if (subscription.getStatus() != UserSubscriptionEntity.SubscriptionStatus.PENDING) {
            throw new RuntimeException("Only pending subscriptions can be cancelled");
        }

        // Delete the subscription
        userSubscriptionRepository.delete(subscription);
    }
}
