package com.x.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record MarketplaceCartItemRequest(
        @NotNull @Positive Long productId,
        @NotNull @Positive Long storeId,
        @NotNull @Positive Long variantId,
        @NotBlank @Size(max = 200) String productName,
        String thumbnail,
        @NotNull @DecimalMin("0.00") BigDecimal unitPrice,
        @NotBlank @Pattern(regexp = "[A-Za-z]{3}") String currencyCode,
        @NotNull @Positive Integer quantity) {
}
