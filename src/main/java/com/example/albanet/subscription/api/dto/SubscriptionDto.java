package com.example.albanet.subscription.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SubscriptionDto {

    private Long id;
    private Long catalogId;
    private Integer monthsPurchased;
    private BigDecimal priceAtStart;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private boolean active;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCatalogId() { return catalogId; }
    public void setCatalogId(Long catalogId) { this.catalogId = catalogId; }

    public Integer getMonthsPurchased() { return monthsPurchased; }
    public void setMonthsPurchased(Integer monthsPurchased) { this.monthsPurchased = monthsPurchased; }

    public BigDecimal getPriceAtStart() { return priceAtStart; }
    public void setPriceAtStart(BigDecimal priceAtStart) { this.priceAtStart = priceAtStart; }

    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }

    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
