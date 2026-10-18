package com.x.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** Asks what discount a number of points gives on an order, without spending them. */
public record LoyaltyQuoteRequest(
        @NotNull @Positive Long businessId,
        @NotNull @Positive Long customerId,
        @NotNull @Positive Long points,
        @NotBlank @Pattern(regexp = "[A-Za-z]{3}") String currencyCode,
        @Positive BigDecimal usdToKhrRate) {
}
