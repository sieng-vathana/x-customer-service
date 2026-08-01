package com.x.customer.dto;

import com.x.customer.entity.Customer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CustomerResponse(
        Long id, Long businessId, Long storeId, String customerCode, String fullName, String phone,
        String email, String gender, LocalDate dateOfBirth, String note, Integer status,
        List<CustomerAddressResponse> addresses, LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static CustomerResponse from(Customer customer) {
        List<CustomerAddressResponse> addresses = customer.getAddresses() == null
                ? List.of()
                : customer.getAddresses().stream().map(CustomerAddressResponse::from).toList();
        return new CustomerResponse(
                customer.getId(), customer.getBusinessId(), customer.getStoreId(), customer.getCustomerCode(), customer.getFullName(),
                customer.getPhone(), customer.getEmail(), customer.getGender(), customer.getDateOfBirth(),
                customer.getNote(), customer.getStatus(), addresses, customer.getCreatedAt(), customer.getUpdatedAt());
    }
}
