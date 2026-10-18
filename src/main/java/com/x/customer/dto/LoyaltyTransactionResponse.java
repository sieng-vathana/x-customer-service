package com.x.customer.dto;

import com.x.customer.entity.LoyaltyTransaction;

import java.time.LocalDateTime;

public record LoyaltyTransactionResponse(
        Long id, Long orderId, String orderNo, String type, long points, long balanceAfter, LocalDateTime createdAt) {

    public static LoyaltyTransactionResponse from(LoyaltyTransaction transaction) {
        return new LoyaltyTransactionResponse(
                transaction.getId(), transaction.getOrderId(), transaction.getOrderNo(), transaction.getType().name(),
                transaction.getPoints(), transaction.getBalanceAfter(), transaction.getCreatedAt());
    }
}
