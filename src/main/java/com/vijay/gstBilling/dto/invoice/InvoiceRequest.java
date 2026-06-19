// dto/invoice/InvoiceRequest.java
package com.vijay.gstBilling.dto.invoice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter @Setter
public class InvoiceRequest {
    @NotNull private UUID customerId;
    @NotNull private LocalDate invoiceDate;
    private LocalDate dueDate;
    @NotNull private Boolean interState;
    private String notes;

    @NotEmpty @Valid
    private List<InvoiceItemRequest> items;
}