package com.x.customer.dto;

import java.math.BigDecimal;

/** The discount, in the order currency, that the points are worth. */
public record LoyaltyQuoteResponse(long points, BigDecimal discountAmount) {
}
