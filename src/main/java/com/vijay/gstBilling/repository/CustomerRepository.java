package com.vijay.gstBilling.repository;

import com.vijay.gstBilling.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Page<Customer> findByUserId(UUID userId, Pageable pageable);
    Page<Customer> findByUserIdAndNameContainingIgnoreCase(UUID userId, String name, Pageable pageable);
    Optional<Customer> findByIdAndUserId(UUID id, UUID userId);
}