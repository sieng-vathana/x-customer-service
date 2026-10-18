package com.x.customer.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record LoyaltyProgramRequest(
        @NotNull Boolean enabled,
        @NotNull @DecimalMin(value = "0.00", message = "Points per dollar cannot be negative")
        @DecimalMax(value = "1000.00", message = "Points per dollar is too large") BigDecimal pointsPerUsd,
        @NotNull @DecimalMin(value = "0.0001", message = "A point must be worth more than $0")
        @DecimalMax(value = "1000.00", message = "Point value is too large") BigDecimal pointValueUsd,
        @NotNull @Min(value = 1, message = "Minimum points to redeem must be at least 1")
        @Max(value = 1000000, message = "Minimum points to redeem is too large") Long minRedeemPoints,
        @NotNull @DecimalMin(value = "0.00", message = "Member discount cannot be negative")
        @DecimalMax(value = "100.00", message = "Member discount cannot exceed 100%") BigDecimal memberDiscountPercent) {
}
