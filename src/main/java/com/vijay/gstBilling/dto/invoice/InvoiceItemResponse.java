// dto/invoice/InvoiceItemResponse.java
package com.vijay.gstBilling.dto.invoice;

import com.vijay.gstBilling.entity.InvoiceItem;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class InvoiceItemResponse {
    private final UUID id;
    private final UUID productId;
    private final String description;
    private final BigDecimal quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal gstRate;
    private final BigDecimal cgst;
    private final BigDecimal sgst;
    private final BigDecimal igst;
    private final BigDecimal lineTotal;

    public InvoiceItemResponse(InvoiceItem i) {
        this.id = i.getId();
        this.productId = i.getProduct().getId();
        this.description = i.getDescription();
        this.quantity = i.getQuantity();
        this.unitPrice = i.getUnitPrice();
        this.gstRate = i.getGstRate();
        this.cgst = i.getCgst();
        this.sgst = i.getSgst();
        this.igst = i.getIgst();
        this.lineTotal = i.getLineTotal();
    }
}