package com.x.customer.service;

import com.x.customer.dto.MarketplaceCartItemRequest;
import com.x.customer.entity.MarketplaceCart;
import com.x.customer.entity.MarketplaceCartItem;
import com.x.customer.repository.MarketplaceCartRepository;
import com.x.redis.cache.CacheNames;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class MarketplaceCartService {
    private final MarketplaceCartRepository cartRepository;

    @CacheEvict(cacheNames = CacheNames.MARKETPLACE_USER_SUMMARY, key = "#userId")
    @Transactional
    public void upsertItem(Long userId, MarketplaceCartItemRequest request) {
        MarketplaceCart cart = cartRepository.findByUserId(userId).orElseGet(() -> MarketplaceCart.builder()
                .userId(userId).items(new ArrayList<>()).build());
        MarketplaceCartItem item = cart.getItems().stream()
                .filter(existing -> existing.getVariantId().equals(request.variantId()))
                .findFirst()
                .orElseGet(() -> {
                    MarketplaceCartItem created = new MarketplaceCartItem();
                    created.setCart(cart);
                    cart.getItems().add(created);
                    return created;
                });
        item.setProductId(request.productId());
        item.setStoreId(request.storeId());
        item.setVariantId(request.variantId());
        item.setProductName(request.productName().trim());
        item.setThumbnail(request.thumbnail());
        item.setUnitPrice(request.unitPrice());
        item.setCurrencyCode(request.currencyCode().toUpperCase(java.util.Locale.ROOT));
        item.setQuantity(request.quantity());
        cartRepository.save(cart);
    }

    @CacheEvict(cacheNames = CacheNames.MARKETPLACE_USER_SUMMARY, key = "#userId")
    @Transactional
    public void removeItem(Long userId, Long variantId) {
        MarketplaceCart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));
        boolean removed = cart.getItems().removeIf(item -> item.getVariantId().equals(variantId));
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found");
        }
        cartRepository.save(cart);
    }

    @CacheEvict(cacheNames = CacheNames.MARKETPLACE_USER_SUMMARY, key = "#userId")
    @Transactional
    public void removeStoreItems(Long userId, Long storeId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().removeIf(item -> item.getStoreId().equals(storeId));
            cartRepository.save(cart);
        });
    }
}
