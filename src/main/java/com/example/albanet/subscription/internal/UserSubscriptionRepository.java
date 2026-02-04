package com.example.albanet.subscription.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscriptionEntity, Long> {

    List<UserSubscriptionEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<UserSubscriptionEntity> findByUserIdAndStatus(Long userId, UserSubscriptionEntity.SubscriptionStatus status);

    List<UserSubscriptionEntity> findByUserIdAndCategoryAndStatus(Long userId, String category, UserSubscriptionEntity.SubscriptionStatus status);
}
