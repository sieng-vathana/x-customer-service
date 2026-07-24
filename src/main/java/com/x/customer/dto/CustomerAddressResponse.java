package com.x.customer.dto;

import com.x.customer.entity.CustomerAddress;

import java.math.BigDecimal;

public record CustomerAddressResponse(
        Long id, String addressName, String receiverName, String phone,
        String provinceCity, String district, String commune, String village,
        String streetAddress, String landmark, String deliveryInstructions,
        BigDecimal latitude, BigDecimal longitude, Boolean isDefault) {
    public static CustomerAddressResponse from(CustomerAddress address) {
        return new CustomerAddressResponse(
                address.getId(), address.getAddressName(), address.getReceiverName(), address.getPhone(),
                address.getProvinceCity(), address.getDistrict(), address.getCommune(), address.getVillage(),
                address.getStreetAddress(), address.getLandmark(), address.getDeliveryInstructions(),
                address.getLatitude(), address.getLongitude(), address.getIsDefault());
    }
}
