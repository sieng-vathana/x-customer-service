package com.x.customer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** A business's loyalty rules: how customers earn and redeem points, and the member discount. */
@Entity
@Table(name = "loyalty_programs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyProgram {

    @Id
    @Column(name = "business_id")
    private Long businessId;

    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = false;

    /** Points earned for each US dollar paid; riel totals are converted at the business rate. */
    @Builder.Default
    @Column(name = "points_per_usd", nullable = false, precision = 10, scale = 2)
    private BigDecimal pointsPerUsd = BigDecimal.ONE;

    /** What one point is worth in US dollars when it is redeemed. */
    @Builder.Default
    @Column(name = "point_value_usd", nullable = false, precision = 10, scale = 4)
    private BigDecimal pointValueUsd = new BigDecimal("0.1000");

    @Builder.Default
    @Column(name = "min_redeem_points", nullable = false)
    private Long minRedeemPoints = 10L;

    /** Percent off every item for saved customers at the POS; 0 turns member prices off. */
    @Builder.Default
    @Column(name = "member_discount_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal memberDiscountPercent = BigDecimal.ZERO;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
