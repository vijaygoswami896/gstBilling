package com.vijay.gstBilling.repository;

import com.vijay.gstBilling.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    List<Invoice> findByUserId(UUID userId);
    Optional<Invoice> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT MAX(i.invoiceNumber) FROM Invoice i WHERE i.user.id = :userId")
    Optional<String> findMaxInvoiceNumberByUserId(UUID userId);
}
