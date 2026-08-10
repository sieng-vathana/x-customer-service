package com.x.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record MarketplaceCustomerRequest(
        @NotNull @Positive Long userId,
        @NotNull @Positive Long businessId,
        @Positive Long storeId,
        @NotBlank @Size(max = 160) String fullName,
        @Size(max = 40) String phone,
        @Size(max = 160) String email) {
}
