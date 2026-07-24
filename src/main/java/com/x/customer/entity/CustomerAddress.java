package com.x.customer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "customer_addresses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    private Customer customer;

    @Column(name = "address_name", length = 80)
    private String addressName;
    @Column(name = "receiver_name", length = 160)
    private String receiverName;
    @Column(length = 40)
    private String phone;
    @Column(name = "province_city", length = 100)
    private String provinceCity;
    @Column(length = 100)
    private String district;
    @Column(length = 100)
    private String commune;
    @Column(length = 100)
    private String village;
    @Column(name = "street_address", length = 255)
    private String streetAddress;
    @Column(length = 255)
    private String landmark;
    @Column(name = "delivery_instructions", length = 500)
    private String deliveryInstructions;
    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;
    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;
}
