package com.x.customer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** A completed order: spends the points it redeemed and earns points on the amount paid. */
public record LoyaltySettleRequest(
        @NotNull @Positive Long businessId,
        @NotNull @Positive Long customerId,
        @Size(max = 64) String orderNo,
        @NotNull @DecimalMin("0.00") BigDecimal amount,
        @NotBlank @Pattern(regexp = "[A-Za-z]{3}") String currencyCode,
        @Positive BigDecimal usdToKhrRate,
        @PositiveOrZero Long redeemPoints) {
}
