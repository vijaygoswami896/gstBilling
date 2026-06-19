package com.vijay.gstBilling.repository;

import com.vijay.gstBilling.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepositoryOld extends JpaRepository<Product, UUID> {
    List<Product> findByUserId(UUID userId);
    Optional<Product> findByIdAndUserId(UUID id, UUID userId);
}
