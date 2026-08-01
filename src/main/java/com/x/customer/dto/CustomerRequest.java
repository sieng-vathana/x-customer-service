package com.x.customer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CustomerRequest(
        @NotNull @Positive Long businessId,
        @Positive Long storeId,
        @Size(max = 40) String customerCode,
        @NotBlank @Size(max = 160) String fullName,
        @Size(max = 40) String phone,
        @Email @Size(max = 160) String email,
        @Size(max = 20) String gender,
        LocalDate dateOfBirth,
        @Size(max = 500) String note,
        List<@Valid CustomerAddressRequest> addresses) {
}
