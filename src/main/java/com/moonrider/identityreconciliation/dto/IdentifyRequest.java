package com.moonrider.identityreconciliation.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdentifyRequest {

    @Email(message = "Invalid email format")
    private String email;

    private String phoneNumber;

    public boolean isValid() {
        return (email != null && !email.trim().isEmpty()) ||
                (phoneNumber != null && !phoneNumber.trim().isEmpty());
    }
}