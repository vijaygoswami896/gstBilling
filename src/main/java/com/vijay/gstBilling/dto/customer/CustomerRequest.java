package com.vijay.gstBilling.dto.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CustomerRequest {
    @NotBlank private String name;
    @Email @NotBlank private String email;
    private String phone;
    private String gstin;
    @NotBlank private String address;
}