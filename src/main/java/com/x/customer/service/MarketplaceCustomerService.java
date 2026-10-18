package com.x.customer.service;

import com.x.customer.dto.MarketplaceCustomerRequest;
import com.x.customer.dto.MarketplaceCustomerResponse;
import com.x.customer.entity.Customer;
import com.x.customer.repository.CustomerRepository;
import com.x.redis.cache.CacheNames;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarketplaceCustomerService {
    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public MarketplaceCustomerResponse findProfile(Long userId) {
        return customerRepository.findAllByUserIdAndStatusOrderByUpdatedAtDesc(userId, 1).stream()
                .findFirst()
                .map(MarketplaceCustomerResponse::from)
                .orElse(null);
    }

    @CacheEvict(cacheNames = CacheNames.MARKETPLACE_USER_SUMMARY, key = "#request.userId()")
    @Transactional
    public MarketplaceCustomerResponse upsert(MarketplaceCustomerRequest request) {
        Customer customer = customerRepository.findByUserIdAndBusinessId(request.userId(), request.businessId())
                .orElseGet(() -> Customer.builder()
                        .userId(request.userId())
                        .businessId(request.businessId())
                        .customerCode("ONLINE-" + request.userId())
                        .status(1)
                        .build());
        customer.setStoreId(request.storeId());
        customer.setFullName(request.fullName().trim());
        customer.setPhone(trim(request.phone()));
        customer.setEmail(trim(request.email()));
        customer.setStatus(1);
        return MarketplaceCustomerResponse.from(customerRepository.save(customer));
    }

    @CacheEvict(cacheNames = CacheNames.MARKETPLACE_USER_SUMMARY, key = "#userId")
    @Transactional
    public void deleteFailedRegistration(Long userId, Long businessId) {
        customerRepository.findByUserIdAndBusinessId(userId, businessId).ifPresent(customerRepository::delete);
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
