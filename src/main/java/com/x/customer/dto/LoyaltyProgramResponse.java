package com.x.customer.dto;

import com.x.customer.entity.LoyaltyProgram;

import java.math.BigDecimal;

public record LoyaltyProgramResponse(
        Long businessId,
        boolean enabled,
        BigDecimal pointsPerUsd,
        BigDecimal pointValueUsd,
        long minRedeemPoints,
        BigDecimal memberDiscountPercent) {

    public static LoyaltyProgramResponse from(LoyaltyProgram program) {
        return new LoyaltyProgramResponse(
                program.getBusinessId(), Boolean.TRUE.equals(program.getEnabled()), program.getPointsPerUsd(),
                program.getPointValueUsd(), program.getMinRedeemPoints(), program.getMemberDiscountPercent());
    }
}
