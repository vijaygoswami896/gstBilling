package com.vijay.gstBilling.dto.email;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EmailVerificationMessage implements Serializable {
    private String to;
    private String name;
    private String verifyLink;
}
