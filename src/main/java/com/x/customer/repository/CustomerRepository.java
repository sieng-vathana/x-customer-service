package com.x.customer.repository;

import com.x.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Page<Customer> findByBusinessIdAndStatus(Long businessId, Integer status, Pageable pageable);
    Optional<Customer> findByIdAndBusinessId(Long id, Long businessId);
    boolean existsByBusinessIdAndCustomerCode(Long businessId, String customerCode);
    List<Customer> findAllByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, Integer status);
    Optional<Customer> findByUserIdAndBusinessId(Long userId, Long businessId);
}
