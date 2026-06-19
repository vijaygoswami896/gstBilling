// service/InvoiceService.java  (full replace — adds getAll, getById, updateStatus, delete)
package com.vijay.gstBilling.service;

import com.vijay.gstBilling.dto.invoice.InvoiceItemRequest;
import com.vijay.gstBilling.dto.invoice.InvoiceRequest;
import com.vijay.gstBilling.dto.invoice.InvoiceResponse;
import com.vijay.gstBilling.entity.*;
import com.vijay.gstBilling.exception.BadRequestException;
import com.vijay.gstBilling.exception.ResourceNotFoundException;
import com.vijay.gstBilling.exception.UnauthorizedException;
import com.vijay.gstBilling.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class InvoiceService {

    private static final Set<String> VALID_STATUSES = Set.of("DRAFT","SENT","PAID","CANCELLED");

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          CustomerRepository customerRepository,
                          ProductRepository productRepository,
                          UserRepository userRepository) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public InvoiceResponse create(InvoiceRequest request) {
        User user = currentUser();

        Customer customer = customerRepository
                .findByIdAndUserId(request.getCustomerId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        String invoiceNumber = generateInvoiceNumber(user.getId());

        Invoice invoice = Invoice.builder()
                .user(user)
                .customer(customer)
                .invoiceNumber(invoiceNumber)
                .invoiceDate(request.getInvoiceDate())
                .dueDate(request.getDueDate())
                .interState(request.getInterState())
                .notes(request.getNotes())
                .subtotal(BigDecimal.ZERO)
                .totalGst(BigDecimal.ZERO)
                .grandTotal(BigDecimal.ZERO)
                .status("DRAFT")
                .items(new ArrayList<>())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;

        for (InvoiceItemRequest itemReq : request.getItems()) {
            Product product = productRepository
                    .findByIdAndUserId(itemReq.getProductId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found: " + itemReq.getProductId()));

            BigDecimal lineTotal = itemReq.getQuantity()
                    .multiply(itemReq.getUnitPrice())
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal gstAmount = lineTotal
                    .multiply(itemReq.getGstRate())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            BigDecimal cgst, sgst, igst;
            if (request.getInterState()) {
                igst = gstAmount;
                cgst = BigDecimal.ZERO.setScale(2);
                sgst = BigDecimal.ZERO.setScale(2);
            } else {
                igst = BigDecimal.ZERO.setScale(2);
                cgst = gstAmount.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                sgst = gstAmount.subtract(cgst);
            }

            InvoiceItem item = InvoiceItem.builder()
                    .invoice(invoice)
                    .product(product)
                    .description(itemReq.getDescription())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .gstRate(itemReq.getGstRate())
                    .cgst(cgst).sgst(sgst).igst(igst)
                    .lineTotal(lineTotal)
                    .build();

            invoice.getItems().add(item);
            subtotal = subtotal.add(lineTotal);
            totalGst = totalGst.add(gstAmount);
        }

        invoice.setSubtotal(subtotal);
        invoice.setTotalGst(totalGst);
        invoice.setGrandTotal(subtotal.add(totalGst));

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice created: {} for user: {}", saved.getInvoiceNumber(), user.getEmail());
        return new InvoiceResponse(saved);
    }

    public List<InvoiceResponse> getAll() {
        UUID userId = currentUser().getId();
        return invoiceRepository.findByUserId(userId)
                .stream().map(InvoiceResponse::new).toList();
    }

    public InvoiceResponse getById(UUID id) {
        UUID userId = currentUser().getId();
        Invoice invoice = invoiceRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        return new InvoiceResponse(invoice);
    }

    @Transactional
    public InvoiceResponse updateStatus(UUID id, String status) {
        if (!VALID_STATUSES.contains(status)) {
            throw new BadRequestException("Invalid status. Must be one of: " + VALID_STATUSES);
        }
        UUID userId = currentUser().getId();
        Invoice invoice = invoiceRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        invoice.setStatus(status);
        log.info("Invoice {} status updated to: {}", id, status);
        return new InvoiceResponse(invoiceRepository.save(invoice));
    }

    @Transactional
    public void delete(UUID id) {
        UUID userId = currentUser().getId();
        Invoice invoice = invoiceRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        if (!"DRAFT".equals(invoice.getStatus())) {
            throw new BadRequestException("Only DRAFT invoices can be deleted");
        }
        invoiceRepository.delete(invoice);
        log.info("Invoice deleted: {}", id);
    }

    private String generateInvoiceNumber(UUID userId) {
        String max = invoiceRepository.findMaxInvoiceNumberByUserId(userId).orElse(null);
        int next = 1;
        if (max != null) {
            try {
                next = Integer.parseInt(max.replace("INV-", "")) + 1;
            } catch (NumberFormatException ignored) {}
        }
        return "INV-" + String.format("%04d", next);
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }
}