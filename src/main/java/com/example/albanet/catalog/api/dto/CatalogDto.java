package com.example.albanet.catalog.api.dto;

import com.example.albanet.catalog.internal.enums.CatalogCode;
import com.example.albanet.catalog.internal.enums.ProductType;

import java.math.BigDecimal;

public class CatalogDto {

    private Long id;
    private ProductType productType;
    private CatalogCode code;
    private String displayName;
    private BigDecimal pricePerMonth;
    private boolean active;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ProductType getProductType() { return productType; }
    public void setProductType(ProductType productType) { this.productType = productType; }

    public CatalogCode getCode() { return code; }
    public void setCode(CatalogCode code) { this.code = code; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public BigDecimal getPricePerMonth() { return pricePerMonth; }
    public void setPricePerMonth(BigDecimal pricePerMonth) { this.pricePerMonth = pricePerMonth; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
