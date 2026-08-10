package com.x.customer.controller;

import com.sharedlib.response.ApiResponse;
import com.x.customer.dto.MarketplaceSummaryResponse;
import com.x.customer.dto.MarketplaceCustomerRequest;
import com.x.customer.dto.MarketplaceCustomerResponse;
import com.x.customer.service.MarketplaceSummaryService;
import com.x.customer.service.MarketplaceCustomerService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/marketplace")
@RequiredArgsConstructor
@Validated
public class MarketplaceController {
    private final MarketplaceSummaryService summaryService;
    private final MarketplaceCustomerService customerService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<MarketplaceSummaryResponse>> getSummary(
            @RequestParam @Positive Long userId) {
        return ResponseEntity.ok(ApiResponse.success(200, summaryService.getSummary(userId)));
    }

    @PostMapping("/customer")
    public ResponseEntity<ApiResponse<MarketplaceCustomerResponse>> upsertCustomer(
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody MarketplaceCustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(200, customerService.upsert(request)));
    }
}
