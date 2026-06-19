// dto/product/ProductResponse.java
package com.vijay.gstBilling.dto.product;

import com.vijay.gstBilling.entity.Product;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
public class ProductResponse {
    private final UUID id;
    private final String name;
    private final String hsn;
    private final String unit;
    private final BigDecimal price;
    private final BigDecimal gstRate;
    private final Instant createdAt;

    public ProductResponse(Product p) {
        this.id = p.getId();
        this.name = p.getName();
        this.hsn = p.getHsn();
        this.unit = p.getUnit();
        this.price = p.getPrice();
        this.gstRate = p.getGstRate();
        this.createdAt = p.getCreatedAt();
    }
}