package com.x.customer.service;

import com.x.customer.dto.*;
import com.x.customer.entity.Customer;
import com.x.customer.entity.CustomerAddress;
import com.x.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        String code = normalizeCode(request.customerCode());
        if (!StringUtils.hasText(code)) {
            code = "CUS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        }
        ensureCodeAvailable(request.businessId(), code);
        Customer customer = Customer.builder()
                .businessId(request.businessId()).storeId(request.storeId())
                .customerCode(code).fullName(request.fullName().trim())
                .phone(trim(request.phone())).email(trim(request.email())).gender(trim(request.gender()))
                .dateOfBirth(request.dateOfBirth()).note(trim(request.note())).status(1).build();
        customer.replaceAddresses(toAddresses(request.addresses()));
        validateDefaultAddress(customer.getAddresses());
        return CustomerResponse.from(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> list(Long businessId, int page, int size) {
        return customerRepository.findByBusinessIdAndStatus(
                        businessId, 1, PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(CustomerResponse::from);
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(Long businessId, Long id) {
        return CustomerResponse.from(findOwned(businessId, id));
    }

    @Transactional
    public CustomerResponse update(Long businessId, Long id, CustomerRequest request) {
        if (!businessId.equals(request.businessId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "businessId cannot be changed");
        }
        Customer customer = findOwned(businessId, id);
        String code = normalizeCode(request.customerCode());
        if (StringUtils.hasText(code) && !code.equals(customer.getCustomerCode())) {
            ensureCodeAvailable(businessId, code);
            customer.setCustomerCode(code);
        }
        customer.setFullName(request.fullName().trim());
        customer.setStoreId(request.storeId());
        customer.setPhone(trim(request.phone()));
        customer.setEmail(trim(request.email()));
        customer.setGender(trim(request.gender()));
        customer.setDateOfBirth(request.dateOfBirth());
        customer.setNote(trim(request.note()));
        if (request.addresses() != null) {
            customer.replaceAddresses(toAddresses(request.addresses()));
            validateDefaultAddress(customer.getAddresses());
        }
        return CustomerResponse.from(customerRepository.save(customer));
    }

    @Transactional
    public void deactivate(Long businessId, Long id) {
        Customer customer = findOwned(businessId, id);
        customer.setStatus(0);
        customerRepository.save(customer);
    }

    private Customer findOwned(Long businessId, Long id) {
        return customerRepository.findByIdAndBusinessId(id, businessId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    private void ensureCodeAvailable(Long businessId, String code) {
        if (customerRepository.existsByBusinessIdAndCustomerCode(businessId, code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer code already exists");
        }
    }

    private List<CustomerAddress> toAddresses(List<CustomerAddressRequest> requests) {
        if (requests == null) return List.of();
        return requests.stream().map(request -> CustomerAddress.builder()
                .addressName(trim(request.addressName())).receiverName(trim(request.receiverName()))
                .phone(trim(request.phone())).provinceCity(trim(request.provinceCity()))
                .district(trim(request.district())).commune(trim(request.commune())).village(trim(request.village()))
                .streetAddress(trim(request.streetAddress())).landmark(trim(request.landmark()))
                .deliveryInstructions(trim(request.deliveryInstructions())).latitude(request.latitude())
                .longitude(request.longitude()).isDefault(Boolean.TRUE.equals(request.isDefault())).build()).toList();
    }

    private void validateDefaultAddress(List<CustomerAddress> addresses) {
        if (addresses.stream().filter(address -> Boolean.TRUE.equals(address.getIsDefault())).count() > 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only one customer address can be default");
        }
    }

    private String normalizeCode(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : null;
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
