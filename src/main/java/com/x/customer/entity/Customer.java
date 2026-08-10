package com.x.customer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_customer_business_code", columnNames = {"business_id", "customer_code"}),
        @UniqueConstraint(name = "uk_customer_business_user", columnNames = {"business_id", "user_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Links an e-commerce account to the business customer record. */
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    /** Optional originating/default store. Customers remain visible across their business. */
    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "customer_code", nullable = false, length = 40)
    private String customerCode;

    @Column(name = "full_name", nullable = false, length = 160)
    private String fullName;

    @Column(length = 40)
    private String phone;

    @Column(length = 160)
    private String email;

    @Column(length = 20)
    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 500)
    private String note;

    @Column(nullable = false)
    private Integer status;

    @Builder.Default
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerAddress> addresses = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void replaceAddresses(List<CustomerAddress> replacement) {
        addresses.clear();
        if (replacement != null) {
            replacement.forEach(address -> {
                address.setCustomer(this);
                addresses.add(address);
            });
        }
    }
}
