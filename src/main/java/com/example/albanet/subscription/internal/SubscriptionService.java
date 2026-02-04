package com.example.albanet.subscription.internal;

import com.example.albanet.subscription.api.dto.SubscriptionDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository repository;
    private final SubscriptionMapper mapper;

    public SubscriptionService(SubscriptionRepository repository,
                               SubscriptionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public SubscriptionDto createSubscription(SubscriptionEntity subscription) {

        repository.findByUserIdAndCatalogIdAndActiveTrue(
                subscription.getUserId(),
                subscription.getCatalogId()
        ).ifPresent(s -> {
            throw new IllegalStateException("Active subscription already exists for this package");
        });

        subscription.setStartAt(LocalDateTime.now());
        subscription.setEndAt(
                subscription.getStartAt().plusMonths(subscription.getMonthsPurchased())
        );
        subscription.setActive(true);

        SubscriptionEntity saved = repository.save(subscription);
        return mapper.toDto(saved);
    }
}
