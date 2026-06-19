package com.vijay.gstBilling.dto.customer;

import com.vijay.gstBilling.entity.Customer;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class CustomerResponse {
    private final UUID id;
    private final String name;
    private final String email;
    private final String phone;
    private final String gstin;
    private final String address;
    private final Instant createdAt;

    public CustomerResponse(Customer c) {
        this.id = c.getId();
        this.name = c.getName();
        this.email = c.getEmail();
        this.phone = c.getPhone();
        this.gstin = c.getGstin();
        this.address = c.getAddress();
        this.createdAt = c.getCreatedAt();
    }
}