package com.x.customer.repository;

import com.x.customer.entity.MarketplaceFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarketplaceFavoriteRepository extends JpaRepository<MarketplaceFavorite, Long> {
    List<MarketplaceFavorite> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    void deleteByUserIdAndProductId(Long userId, Long productId);
}
