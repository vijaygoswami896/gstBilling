// dto/invoice/InvoiceResponse.java
package com.vijay.gstBilling.dto.invoice;

import com.vijay.gstBilling.entity.Invoice;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
public class InvoiceResponse {
    private final UUID id;
    private final UUID customerId;
    private final String customerName;
    private final String invoiceNumber;
    private final LocalDate invoiceDate;
    private final LocalDate dueDate;
    private final boolean interState;
    private final BigDecimal subtotal;
    private final BigDecimal totalGst;
    private final BigDecimal grandTotal;
    private final String status;
    private final String notes;
    private final List<InvoiceItemResponse> items;
    private final Instant createdAt;

    public InvoiceResponse(Invoice inv) {
        this.id = inv.getId();
        this.customerId = inv.getCustomer().getId();
        this.customerName = inv.getCustomer().getName();
        this.invoiceNumber = inv.getInvoiceNumber();
        this.invoiceDate = inv.getInvoiceDate();
        this.dueDate = inv.getDueDate();
        this.interState = inv.isInterState();
        this.subtotal = inv.getSubtotal();
        this.totalGst = inv.getTotalGst();
        this.grandTotal = inv.getGrandTotal();
        this.status = inv.getStatus();
        this.notes = inv.getNotes();
        this.items = inv.getItems().stream().map(InvoiceItemResponse::new).toList();
        this.createdAt = inv.getCreatedAt();
    }
}