package com.x.customer.repository;

import com.x.customer.entity.Customer;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query("SELECT c FROM Customer c WHERE c.status = :status AND (c.businessId = :businessId OR c.id = 0)")
    Page<Customer> findByBusinessIdAndStatusIncludingWalkIn(
            @Param("businessId") Long businessId,
            @Param("status") Integer status,
            Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.id = :id AND (c.businessId = :businessId OR c.id = 0)")
    Optional<Customer> findByIdAndBusinessIdIncludingWalkIn(
            @Param("id") Long id,
            @Param("businessId") Long businessId);
    boolean existsByBusinessIdAndCustomerCode(Long businessId, String customerCode);
    List<Customer> findAllByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, Integer status);
    Optional<Customer> findByUserIdAndBusinessId(Long userId, Long businessId);

    Optional<Customer> findFirstByBusinessIdAndPhoneNormalizedAndStatusOrderByUpdatedAtDesc(
            Long businessId, String phoneNormalized, Integer status);

    /** Locks the row so concurrent orders cannot spend the same points twice. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Customer c WHERE c.id = :id AND c.businessId = :businessId")
    Optional<Customer> lockByIdAndBusinessId(@Param("id") Long id, @Param("businessId") Long businessId);

    List<Customer> findByIdGreaterThanAndPhoneIsNotNullAndPhoneNormalizedIsNullOrderByIdAsc(Long id, Pageable pageable);
}
