// dto/invoice/InvoiceItemRequest.java
package com.vijay.gstBilling.dto.invoice;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter
public class InvoiceItemRequest {
    @NotNull private UUID productId;
    @NotBlank private String description;
    @NotNull @DecimalMin("0.01") private BigDecimal quantity;
    @NotNull @DecimalMin("0.0")  private BigDecimal unitPrice;
    @NotNull @DecimalMin("0.0")  private BigDecimal gstRate;
}