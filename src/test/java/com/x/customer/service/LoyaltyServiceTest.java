package com.x.customer.service;

import com.x.customer.dto.LoyaltyQuoteRequest;
import com.x.customer.dto.LoyaltySettleRequest;
import com.x.customer.entity.Customer;
import com.x.customer.entity.LoyaltyProgram;
import com.x.customer.entity.LoyaltyTransaction;
import com.x.customer.entity.LoyaltyTransactionType;
import com.x.customer.repository.CustomerRepository;
import com.x.customer.repository.LoyaltyProgramRepository;
import com.x.customer.repository.LoyaltyTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoyaltyServiceTest {

    private LoyaltyProgramRepository programRepository;
    private LoyaltyTransactionRepository transactionRepository;
    private CustomerRepository customerRepository;
    private LoyaltyService service;
    private Customer customer;

    @BeforeEach
    void setUp() {
        programRepository = mock(LoyaltyProgramRepository.class);
        transactionRepository = mock(LoyaltyTransactionRepository.class);
        customerRepository = mock(CustomerRepository.class);
        service = new LoyaltyService(programRepository, transactionRepository, customerRepository);
        customer = Customer.builder().id(9L).businessId(2L).status(1).loyaltyPoints(120L).build();
        when(programRepository.findById(2L)).thenReturn(Optional.of(LoyaltyProgram.builder()
                .businessId(2L).enabled(true).pointsPerUsd(new BigDecimal("1.00"))
                .pointValueUsd(new BigDecimal("0.0500")).minRedeemPoints(20L).build()));
        when(customerRepository.findByIdAndBusinessIdIncludingWalkIn(9L, 2L)).thenReturn(Optional.of(customer));
        when(customerRepository.lockByIdAndBusinessId(9L, 2L)).thenReturn(Optional.of(customer));
        when(transactionRepository.findByOrderIdAndType(any(), any())).thenReturn(Optional.empty());
    }

    @Test
    void pointsAreWorthTheirValueInTheOrderCurrency() {
        assertEquals(new BigDecimal("5.00"), service.quote(quote(100, "USD")).discountAmount());
        assertEquals(new BigDecimal("20500"), service.quote(new LoyaltyQuoteRequest(
                2L, 9L, 100L, "KHR", new BigDecimal("4100"))).discountAmount());
    }

    @Test
    void quoteRejectsTooFewOrMorePointsThanTheCustomerHas() {
        ResponseStatusException tooFew = assertThrows(ResponseStatusException.class, () -> service.quote(quote(10, "USD")));
        ResponseStatusException tooMany = assertThrows(ResponseStatusException.class, () -> service.quote(quote(121, "USD")));

        assertEquals("Redeem at least 20 points", tooFew.getReason());
        assertEquals("The customer has only 120 points", tooMany.getReason());
    }

    @Test
    void completedOrderSpendsRedeemedPointsAndEarnsOnTheAmountPaid() {
        var result = service.settle(77L, settle("37.99", "USD", null, 100L));

        assertEquals(100, result.redeemedPoints());
        assertEquals(37, result.earnedPoints());
        assertEquals(57, result.balance());
        assertEquals(57L, customer.getLoyaltyPoints());
    }

    @Test
    void rielOrdersEarnAtTheBusinessRate() {
        var result = service.settle(78L, settle("41000", "KHR", new BigDecimal("4100"), 0L));

        assertEquals(10, result.earnedPoints());
        assertEquals(130, result.balance());
    }

    @Test
    void settlingTheSameOrderAgainChangesNothing() {
        when(transactionRepository.findByOrderIdAndType(77L, LoyaltyTransactionType.REDEEM)).thenReturn(Optional.of(
                LoyaltyTransaction.builder().points(-100L).build()));
        when(transactionRepository.findByOrderIdAndType(77L, LoyaltyTransactionType.EARN)).thenReturn(Optional.of(
                LoyaltyTransaction.builder().points(37L).build()));

        var result = service.settle(77L, settle("37.99", "USD", null, 100L));

        assertEquals(100, result.redeemedPoints());
        assertEquals(37, result.earnedPoints());
        assertEquals(120L, customer.getLoyaltyPoints());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void turnedOffProgramStillSpendsPointsAlreadyGivenAsDiscountButEarnsNothing() {
        when(programRepository.findById(2L)).thenReturn(Optional.of(LoyaltyProgram.builder().businessId(2L).enabled(false).build()));

        var result = service.settle(79L, settle("50.00", "USD", null, 20L));

        assertEquals(20, result.redeemedPoints());
        assertEquals(0, result.earnedPoints());
        assertEquals(100, result.balance());
    }

    private LoyaltyQuoteRequest quote(long points, String currency) {
        return new LoyaltyQuoteRequest(2L, 9L, points, currency, null);
    }

    private LoyaltySettleRequest settle(String amount, String currency, BigDecimal rate, Long redeemPoints) {
        return new LoyaltySettleRequest(2L, 9L, "POS-1", new BigDecimal(amount), currency, rate, redeemPoints);
    }
}
