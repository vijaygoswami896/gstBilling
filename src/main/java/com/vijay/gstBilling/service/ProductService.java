// service/ProductService.java
package com.vijay.gstBilling.service;

import com.vijay.gstBilling.dto.product.ProductRequest;
import com.vijay.gstBilling.dto.product.ProductResponse;
import com.vijay.gstBilling.entity.Product;
import com.vijay.gstBilling.entity.User;
import com.vijay.gstBilling.exception.ResourceNotFoundException;
import com.vijay.gstBilling.exception.UnauthorizedException;
import com.vijay.gstBilling.repository.ProductRepository;
import com.vijay.gstBilling.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository,
                          UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        User user = currentUser();
        Product product = Product.builder()
                .user(user)
                .name(request.getName())
                .hsn(request.getHsn())
                .unit(request.getUnit())
                .price(request.getPrice())
                .gstRate(request.getGstRate())
                .build();
        Product saved = productRepository.save(product);
        log.info("Product created: {} by user: {}", saved.getId(), user.getEmail());
        return new ProductResponse(saved);
    }

    public List<ProductResponse> getAll() {
        UUID userId = currentUser().getId();
        return productRepository.findByUserId(userId)
                .stream().map(ProductResponse::new).toList();
    }

    public ProductResponse getById(UUID id) {
        UUID userId = currentUser().getId();
        Product product = productRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return new ProductResponse(product);
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        UUID userId = currentUser().getId();
        Product product = productRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        product.setName(request.getName());
        product.setHsn(request.getHsn());
        product.setUnit(request.getUnit());
        product.setPrice(request.getPrice());
        product.setGstRate(request.getGstRate());

        log.info("Product updated: {}", id);
        return new ProductResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(UUID id) {
        UUID userId = currentUser().getId();
        Product product = productRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        productRepository.delete(product);
        log.info("Product deleted: {}", id);
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }
}