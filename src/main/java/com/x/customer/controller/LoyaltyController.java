package com.x.customer.controller;

import com.sharedlib.response.ApiResponse;
import com.x.customer.dto.CustomerLoyaltyResponse;
import com.x.customer.dto.CustomerResponse;
import com.x.customer.dto.LoyaltyProgramRequest;
import com.x.customer.dto.LoyaltyProgramResponse;
import com.x.customer.dto.LoyaltyQuoteRequest;
import com.x.customer.dto.LoyaltyQuoteResponse;
import com.x.customer.dto.LoyaltySettleRequest;
import com.x.customer.dto.LoyaltySettleResponse;
import com.x.customer.service.LoyaltyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    @GetMapping("/api/v1/loyalty/program")
    public ResponseEntity<ApiResponse<LoyaltyProgramResponse>> program(@RequestParam @Positive Long businessId) {
        return ResponseEntity.ok(ApiResponse.success(200, loyaltyService.program(businessId)));
    }

    @PutMapping("/api/v1/loyalty/program")
    public ResponseEntity<ApiResponse<LoyaltyProgramResponse>> updateProgram(
            @RequestParam @Positive Long businessId,
            @Valid @RequestBody LoyaltyProgramRequest request) {
        return ResponseEntity.ok(ApiResponse.success(200, "Loyalty settings saved", loyaltyService.updateProgram(businessId, request)));
    }

    @GetMapping("/api/v1/customers/lookup")
    public ResponseEntity<ApiResponse<CustomerResponse>> lookupByPhone(
            @RequestParam @Positive Long businessId,
            @RequestParam @NotBlank String phone) {
        return ResponseEntity.ok(ApiResponse.success(200, loyaltyService.lookupByPhone(businessId, phone)));
    }

    @GetMapping("/api/v1/customers/{id}/loyalty")
    public ResponseEntity<ApiResponse<CustomerLoyaltyResponse>> customerLoyalty(
            @PathVariable @Positive Long id,
            @RequestParam @Positive Long businessId) {
        return ResponseEntity.ok(ApiResponse.success(200, loyaltyService.customerLoyalty(businessId, id)));
    }

    /** Used by the order service while it builds a POS order that redeems points. */
    @PostMapping("/internal/loyalty/quote")
    public ResponseEntity<ApiResponse<LoyaltyQuoteResponse>> quote(@Valid @RequestBody LoyaltyQuoteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(200, loyaltyService.quote(request)));
    }

    /** Used by the order service when an order completes. */
    @PostMapping("/internal/loyalty/orders/{orderId}/settle")
    public ResponseEntity<ApiResponse<LoyaltySettleResponse>> settle(
            @PathVariable @Positive Long orderId,
            @Valid @RequestBody LoyaltySettleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(200, loyaltyService.settle(orderId, request)));
    }
}
