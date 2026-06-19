package com.vijay.gstBilling.service;

import com.vijay.gstBilling.dto.customer.CustomerRequest;
import com.vijay.gstBilling.dto.customer.CustomerResponse;
import com.vijay.gstBilling.entity.Customer;
import com.vijay.gstBilling.entity.User;
import com.vijay.gstBilling.exception.ResourceNotFoundException;
import com.vijay.gstBilling.exception.UnauthorizedException;
import com.vijay.gstBilling.repository.CustomerRepository;
import com.vijay.gstBilling.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public CustomerService(CustomerRepository customerRepository,
                           UserRepository userRepository) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        User user = currentUser();
        Customer customer = Customer.builder()
                .user(user).name(request.getName()).email(request.getEmail())
                .phone(request.getPhone()).gstin(request.getGstin())
                .address(request.getAddress()).build();
        Customer saved = customerRepository.save(customer);
        log.info("Customer created: {} by user: {}", saved.getId(), user.getEmail());
        return new CustomerResponse(saved);
    }

    public Page<CustomerResponse> getAll(String search, Pageable pageable) {
        UUID userId = currentUser().getId();
        log.info("Listing customers for user: {} search: '{}'", userId, search);
        if (search != null && !search.isBlank()) {
            return customerRepository
                    .findByUserIdAndNameContainingIgnoreCase(userId, search.trim(), pageable)
                    .map(CustomerResponse::new);
        }
        return customerRepository.findByUserId(userId, pageable).map(CustomerResponse::new);
    }

    public CustomerResponse getById(UUID id) {
        UUID userId = currentUser().getId();
        Customer customer = customerRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        log.info("Fetched customer: {}", id);
        return new CustomerResponse(customer);
    }

    @Transactional
    public CustomerResponse update(UUID id, CustomerRequest request) {
        UUID userId = currentUser().getId();
        Customer customer = customerRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setGstin(request.getGstin());
        customer.setAddress(request.getAddress());
        log.info("Customer updated: {}", id);
        return new CustomerResponse(customerRepository.save(customer));
    }

    @Transactional
    public void delete(UUID id) {
        UUID userId = currentUser().getId();
        Customer customer = customerRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        customerRepository.delete(customer);
        log.info("Customer deleted: {}", id);
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }
}