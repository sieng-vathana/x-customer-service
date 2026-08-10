package com.x.customer.repository;

import com.x.customer.entity.MarketplaceCart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MarketplaceCartRepository extends JpaRepository<MarketplaceCart, Long> {
    Optional<MarketplaceCart> findByUserId(Long userId);
}
