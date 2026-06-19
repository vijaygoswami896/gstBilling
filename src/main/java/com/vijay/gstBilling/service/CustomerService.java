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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
                .user(user)
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .gstin(request.getGstin())
                .address(request.getAddress())
                .build();
        Customer saved = customerRepository.save(customer);
        log.info("Customer created: {} by user: {}", saved.getId(), user.getEmail());
        return new CustomerResponse(saved);
    }

    public List<CustomerResponse> getAll() {
        UUID userId = currentUser().getId();
        return customerRepository.findByUserId(userId)
                .stream().map(CustomerResponse::new).toList();
    }

    public CustomerResponse getById(UUID id) {
        UUID userId = currentUser().getId();
        Customer customer = customerRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
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
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }
}