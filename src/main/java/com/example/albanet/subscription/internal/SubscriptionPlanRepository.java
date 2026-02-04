package com.example.albanet.subscription.internal;

import com.example.albanet.catalog.internal.enums.CatalogCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlanEntity, Long> {

    List<SubscriptionPlanEntity> findByCategoryAndActiveTrue(String category);

    Optional<SubscriptionPlanEntity> findByCodeAndActiveTrue(CatalogCode code);

    List<SubscriptionPlanEntity> findByActiveTrue();
}
