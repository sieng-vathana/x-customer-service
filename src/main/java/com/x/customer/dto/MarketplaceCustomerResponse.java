package com.x.customer.dto;

import com.x.customer.entity.Customer;

public record MarketplaceCustomerResponse(
        Long id,
        Long userId,
        Long businessId,
        Long storeId,
        String fullName,
        String phone,
        String email) {

    public static MarketplaceCustomerResponse from(Customer customer) {
        return new MarketplaceCustomerResponse(
                customer.getId(), customer.getUserId(), customer.getBusinessId(), customer.getStoreId(),
                customer.getFullName(), customer.getPhone(), customer.getEmail());
    }
}
