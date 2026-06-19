package com.vijay.gstBilling.repository;

import com.vijay.gstBilling.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Page<Product> findByUserId(UUID userId, Pageable pageable);
    Page<Product> findByUserIdAndNameContainingIgnoreCase(UUID userId, String name, Pageable pageable);
    Optional<Product> findByIdAndUserId(UUID id, UUID userId);
}