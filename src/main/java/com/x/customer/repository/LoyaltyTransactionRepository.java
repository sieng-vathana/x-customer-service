package com.x.customer.repository;

import com.x.customer.entity.LoyaltyTransaction;
import com.x.customer.entity.LoyaltyTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {
    Optional<LoyaltyTransaction> findByOrderIdAndType(Long orderId, LoyaltyTransactionType type);

    List<LoyaltyTransaction> findTop20ByBusinessIdAndCustomerIdOrderByCreatedAtDescIdDesc(Long businessId, Long customerId);
}
