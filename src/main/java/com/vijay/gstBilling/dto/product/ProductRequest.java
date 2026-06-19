// dto/product/ProductRequest.java
package com.vijay.gstBilling.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class ProductRequest {
    @NotBlank private String name;
    @NotBlank private String hsn;
    @NotBlank private String unit;

    @NotNull @DecimalMin("0.0")
    private BigDecimal price;

    @NotNull @DecimalMin("0.0")
    private BigDecimal gstRate;
}