package com.x.customer.service;

import com.x.customer.dto.CustomerLoyaltyResponse;
import com.x.customer.dto.CustomerResponse;
import com.x.customer.dto.LoyaltyProgramRequest;
import com.x.customer.dto.LoyaltyProgramResponse;
import com.x.customer.dto.LoyaltyQuoteRequest;
import com.x.customer.dto.LoyaltyQuoteResponse;
import com.x.customer.dto.LoyaltySettleRequest;
import com.x.customer.dto.LoyaltySettleResponse;
import com.x.customer.dto.LoyaltyTransactionResponse;
import com.x.customer.entity.Customer;
import com.x.customer.entity.LoyaltyProgram;
import com.x.customer.entity.LoyaltyTransaction;
import com.x.customer.entity.LoyaltyTransactionType;
import com.x.customer.entity.PhoneNumbers;
import com.x.customer.repository.CustomerRepository;
import com.x.customer.repository.LoyaltyProgramRepository;
import com.x.customer.repository.LoyaltyTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Loyalty points by phone number: customers earn on what they pay and redeem points as an order discount. */
@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private static final Logger log = LoggerFactory.getLogger(LoyaltyService.class);
    private static final BigDecimal DEFAULT_USD_TO_KHR_RATE = new BigDecimal("4000");

    private final LoyaltyProgramRepository programRepository;
    private final LoyaltyTransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public LoyaltyProgramResponse program(Long businessId) {
        return LoyaltyProgramResponse.from(programFor(businessId));
    }

    @Transactional
    public LoyaltyProgramResponse updateProgram(Long businessId, LoyaltyProgramRequest request) {
        LoyaltyProgram program = programFor(businessId);
        program.setEnabled(request.enabled());
        program.setPointsPerUsd(request.pointsPerUsd().setScale(2, RoundingMode.HALF_UP));
        program.setPointValueUsd(request.pointValueUsd().setScale(4, RoundingMode.HALF_UP));
        program.setMinRedeemPoints(request.minRedeemPoints());
        program.setMemberDiscountPercent(request.memberDiscountPercent().setScale(2, RoundingMode.HALF_UP));
        return LoyaltyProgramResponse.from(programRepository.save(program));
    }

    @Transactional(readOnly = true)
    public CustomerResponse lookupByPhone(Long businessId, String phone) {
        String normalized = PhoneNumbers.normalize(phone);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a phone number");
        }
        return customerRepository
                .findFirstByBusinessIdAndPhoneNormalizedAndStatusOrderByUpdatedAtDesc(businessId, normalized, 1)
                .map(CustomerResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No customer has this phone number"));
    }

    @Transactional(readOnly = true)
    public CustomerLoyaltyResponse customerLoyalty(Long businessId, Long customerId) {
        Customer customer = customerRepository.findByIdAndBusinessIdIncludingWalkIn(customerId, businessId)
                .filter(found -> businessId.equals(found.getBusinessId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        return new CustomerLoyaltyResponse(
                customer.getId(),
                customer.getLoyaltyPoints(),
                transactionRepository.findTop20ByBusinessIdAndCustomerIdOrderByCreatedAtDescIdDesc(businessId, customerId)
                        .stream().map(LoyaltyTransactionResponse::from).toList());
    }

    /** The discount the points are worth on an order; nothing is spent until the order completes. */
    @Transactional(readOnly = true)
    public LoyaltyQuoteResponse quote(LoyaltyQuoteRequest request) {
        LoyaltyProgram program = programFor(request.businessId());
        if (!Boolean.TRUE.equals(program.getEnabled())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Loyalty points are turned off for this business");
        }
        Customer customer = customerRepository.findByIdAndBusinessIdIncludingWalkIn(request.customerId(), request.businessId())
                .filter(found -> request.businessId().equals(found.getBusinessId()) && Integer.valueOf(1).equals(found.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        if (request.points() < program.getMinRedeemPoints()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Redeem at least " + program.getMinRedeemPoints() + " points");
        }
        if (request.points() > customer.getLoyaltyPoints()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The customer has only " + customer.getLoyaltyPoints() + " points");
        }
        return new LoyaltyQuoteResponse(request.points(),
                pointsValue(request.points(), program, request.currencyCode(), request.usdToKhrRate()));
    }

    /**
     * Spends the points a completed order redeemed and adds the points it earned. Each order is settled
     * once, so a retried completion returns the same result instead of changing the balance again.
     */
    @Transactional
    public LoyaltySettleResponse settle(Long orderId, LoyaltySettleRequest request) {
        Customer customer = customerRepository.lockByIdAndBusinessId(request.customerId(), request.businessId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        long requested = request.redeemPoints() == null ? 0 : request.redeemPoints();
        long redeemed = transactionRepository.findByOrderIdAndType(orderId, LoyaltyTransactionType.REDEEM)
                .map(existing -> -existing.getPoints())
                .orElseGet(() -> redeem(customer, orderId, request.orderNo(), requested));
        long earned = transactionRepository.findByOrderIdAndType(orderId, LoyaltyTransactionType.EARN)
                .map(LoyaltyTransaction::getPoints)
                .orElseGet(() -> earn(customer, orderId, request));
        customerRepository.save(customer);
        return new LoyaltySettleResponse(redeemed, earned, customer.getLoyaltyPoints());
    }

    private long redeem(Customer customer, Long orderId, String orderNo, long requested) {
        if (requested <= 0) {
            return 0;
        }
        long redeemed = Math.min(requested, customer.getLoyaltyPoints());
        if (redeemed < requested) {
            log.warn("Order {} redeemed {} points but customer {} had only {}", orderNo, requested, customer.getId(), redeemed);
        }
        if (redeemed > 0) {
            record(customer, orderId, orderNo, LoyaltyTransactionType.REDEEM, -redeemed);
        }
        return redeemed;
    }

    private long earn(Customer customer, Long orderId, LoyaltySettleRequest request) {
        LoyaltyProgram program = programRepository.findById(request.businessId()).orElse(null);
        if (program == null || !Boolean.TRUE.equals(program.getEnabled())) {
            return 0;
        }
        BigDecimal amountUsd = "KHR".equalsIgnoreCase(request.currencyCode())
                ? request.amount().divide(rate(request.usdToKhrRate()), 6, RoundingMode.HALF_UP)
                : request.amount();
        long earned = amountUsd.multiply(program.getPointsPerUsd()).setScale(0, RoundingMode.DOWN).longValue();
        if (earned > 0) {
            record(customer, orderId, request.orderNo(), LoyaltyTransactionType.EARN, earned);
        }
        return earned;
    }

    private void record(Customer customer, Long orderId, String orderNo, LoyaltyTransactionType type, long points) {
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + points);
        transactionRepository.save(LoyaltyTransaction.builder()
                .businessId(customer.getBusinessId()).customerId(customer.getId())
                .orderId(orderId).orderNo(orderNo).type(type)
                .points(points).balanceAfter(customer.getLoyaltyPoints())
                .build());
    }

    /** Points are worth whole cents, or whole riel, rounded down. */
    private BigDecimal pointsValue(long points, LoyaltyProgram program, String currencyCode, BigDecimal usdToKhrRate) {
        BigDecimal valueUsd = program.getPointValueUsd().multiply(BigDecimal.valueOf(points));
        return "KHR".equalsIgnoreCase(currencyCode)
                ? valueUsd.multiply(rate(usdToKhrRate)).setScale(0, RoundingMode.DOWN)
                : valueUsd.setScale(2, RoundingMode.DOWN);
    }

    private LoyaltyProgram programFor(Long businessId) {
        return programRepository.findById(businessId)
                .orElseGet(() -> LoyaltyProgram.builder().businessId(businessId).build());
    }

    private static BigDecimal rate(BigDecimal usdToKhrRate) {
        return usdToKhrRate == null || usdToKhrRate.signum() <= 0 ? DEFAULT_USD_TO_KHR_RATE : usdToKhrRate;
    }
}
