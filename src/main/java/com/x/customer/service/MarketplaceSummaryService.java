package com.x.customer.service;

import com.x.customer.dto.MarketplaceCartItemResponse;
import com.x.customer.dto.MarketplaceCustomerResponse;
import com.x.customer.dto.MarketplaceSummaryResponse;
import com.x.customer.entity.MarketplaceCart;
import com.x.customer.repository.MarketplaceCartRepository;
import com.x.customer.repository.MarketplaceFavoriteRepository;
import com.x.redis.cache.CacheNames;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketplaceSummaryService {
    private final MarketplaceFavoriteRepository favoriteRepository;
    private final MarketplaceCartRepository cartRepository;
    private final MarketplaceCustomerService customerService;

    @Cacheable(cacheNames = CacheNames.MARKETPLACE_USER_SUMMARY, key = "#userId")
    @Transactional(readOnly = true)
    public MarketplaceSummaryResponse getSummary(Long userId) {
        MarketplaceCustomerResponse customer = customerService.findProfile(userId);
        List<Long> favoriteIds = favoriteRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(favorite -> favorite.getProductId())
                .toList();
        MarketplaceCart cart = cartRepository.findByUserId(userId).orElse(null);
        List<MarketplaceCartItemResponse> items = cart == null || cart.getItems() == null
                ? List.of()
                : cart.getItems().stream().map(item -> new MarketplaceCartItemResponse(
                        item.getProductId(), item.getStoreId(), item.getVariantId(), item.getProductName(),
                        item.getThumbnail(), item.getUnitPrice(), item.getCurrencyCode(), item.getQuantity(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))).toList();
        BigDecimal subtotal = items.stream().map(MarketplaceCartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String currency = items.isEmpty() ? null : items.get(0).currencyCode();
        int itemCount = items.stream().mapToInt(item -> item.quantity() == null ? 0 : item.quantity()).sum();
        return new MarketplaceSummaryResponse(customer, favoriteIds, favoriteIds.size(), itemCount, subtotal, currency, items);
    }
}
