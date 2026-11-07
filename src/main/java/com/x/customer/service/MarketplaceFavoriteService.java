package com.x.customer.service;

import com.x.customer.dto.MarketplaceFavoriteResponse;
import com.x.customer.entity.MarketplaceFavorite;
import com.x.customer.repository.MarketplaceFavoriteRepository;
import com.x.redis.cache.CacheNames;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketplaceFavoriteService {
    private final MarketplaceFavoriteRepository favoriteRepository;

    @Transactional(readOnly = true)
    public List<MarketplaceFavoriteResponse> list(Long userId) {
        return favoriteRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(favorite -> new MarketplaceFavoriteResponse(
                        favorite.getProductId(), favorite.getStoreId(), favorite.getCreatedAt()))
                .toList();
    }

    @CacheEvict(cacheNames = CacheNames.MARKETPLACE_USER_SUMMARY, key = "#userId")
    @Transactional
    public void add(Long userId, Long productId, Long storeId) {
        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            return;
        }
        favoriteRepository.save(MarketplaceFavorite.builder()
                .userId(userId).productId(productId).storeId(storeId).build());
    }

    @CacheEvict(cacheNames = CacheNames.MARKETPLACE_USER_SUMMARY, key = "#userId")
    @Transactional
    public void remove(Long userId, Long productId) {
        favoriteRepository.deleteByUserIdAndProductId(userId, productId);
    }
}
