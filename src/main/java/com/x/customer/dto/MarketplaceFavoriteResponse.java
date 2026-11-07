package com.x.customer.dto;

import java.time.LocalDateTime;

public record MarketplaceFavoriteResponse(Long productId, Long storeId, LocalDateTime createdAt) {
}
