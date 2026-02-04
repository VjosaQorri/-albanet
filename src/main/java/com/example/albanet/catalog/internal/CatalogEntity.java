package com.example.albanet.catalog.internal;

import com.example.albanet.catalog.internal.enums.CatalogCode;
import com.example.albanet.catalog.internal.enums.ProductType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Entity
@Table(name = "catalog")
public class CatalogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductType productType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private CatalogCode code;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String displayName;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerMonth;

    @Column(nullable = false)
    private boolean active = true;

    public CatalogEntity() {}

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
