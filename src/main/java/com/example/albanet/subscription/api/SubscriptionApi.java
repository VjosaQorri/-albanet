package com.example.albanet.subscription.api;

import com.example.albanet.subscription.api.dto.SubscriptionPlanDto;
import com.example.albanet.subscription.api.dto.UserSubscriptionDto;

import java.util.List;

/**
 * Public API for the Subscription module.
 * Other modules should use this interface instead of accessing internal classes directly.
 */
public interface SubscriptionApi {

    /**
     * Get all plans by category (TV, MOBILE, INTERNET)
     */
    List<SubscriptionPlanDto> getPlansByCategory(String category);

    /**
     * Get a user's active subscription for a category
     */
    UserSubscriptionDto getActiveSubscriptionByCategory(Long userId, String category);

    /**
     * Get a user's pending subscription for a category
     */
    UserSubscriptionDto getPendingSubscriptionByCategory(Long userId, String category);

    /**
     * Subscribe a user to a plan
     * @param userId the user's ID
     * @param planCode the plan code (e.g., "TV_BASIC", "PAKO_S", "WIFI_PREMIUM")
     * @param durationMonths duration in months
     * @return the created subscription
     */
    UserSubscriptionDto subscribe(Long userId, String planCode, Integer durationMonths);

    /**
     * Get all subscriptions for a user
     */
    List<UserSubscriptionDto> getUserSubscriptions(Long userId);

    /**
     * Cancel a pending subscription
     */
    void cancelSubscription(Long subscriptionId, Long userId);
}
