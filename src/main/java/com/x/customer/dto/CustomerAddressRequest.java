package com.x.customer.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CustomerAddressRequest(
        @Size(max = 80) String addressName,
        @Size(max = 160) String receiverName,
        @Size(max = 40) String phone,
        @Size(max = 100) String provinceCity,
        @Size(max = 100) String district,
        @Size(max = 100) String commune,
        @Size(max = 100) String village,
        @Size(max = 255) String streetAddress,
        @Size(max = 255) String landmark,
        @Size(max = 500) String deliveryInstructions,
        @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude,
        Boolean isDefault) {
}
