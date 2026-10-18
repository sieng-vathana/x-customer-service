package com.x.customer.dto;

import java.util.List;

/** A customer's point balance and latest point changes. */
public record CustomerLoyaltyResponse(Long customerId, long points, List<LoyaltyTransactionResponse> recent) {
}
