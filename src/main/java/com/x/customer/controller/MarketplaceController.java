package com.x.customer.controller;

import com.sharedlib.response.ApiResponse;
import com.x.customer.dto.MarketplaceSummaryResponse;
import com.x.customer.dto.MarketplaceCustomerRequest;
import com.x.customer.dto.MarketplaceCustomerResponse;
import com.x.customer.dto.MarketplaceCartItemRequest;
import com.x.customer.dto.MarketplaceFavoriteResponse;
import com.x.customer.service.MarketplaceCartService;
import com.x.customer.service.MarketplaceFavoriteService;
import com.x.customer.service.MarketplaceSummaryService;
import com.x.customer.service.MarketplaceCustomerService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final MarketplaceCartService cartService;
    private final MarketplaceFavoriteService favoriteService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<MarketplaceSummaryResponse>> getSummary(
            @RequestParam @Positive Long userId) {
        return ResponseEntity.ok(ApiResponse.success(200, summaryService.getSummary(userId)));
    }

    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<java.util.List<MarketplaceFavoriteResponse>>> listFavorites(
            @RequestParam @Positive Long userId) {
        return ResponseEntity.ok(ApiResponse.success(200, favoriteService.list(userId)));
    }

    @PostMapping("/favorites")
    public ResponseEntity<ApiResponse<Void>> addFavorite(
            @RequestParam @Positive Long userId,
            @RequestParam @Positive Long productId,
            @RequestParam(required = false) @Positive Long storeId) {
        favoriteService.add(userId, productId, storeId);
        return ResponseEntity.ok(ApiResponse.success(200, null));
    }

    @DeleteMapping("/favorites/{productId}")
    public ResponseEntity<Void> removeFavorite(
            @RequestParam @Positive Long userId,
            @PathVariable @Positive Long productId) {
        favoriteService.remove(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/cart/items")
    public ResponseEntity<ApiResponse<Void>> upsertCartItem(
            @RequestParam @Positive Long userId,
            @jakarta.validation.Valid @RequestBody MarketplaceCartItemRequest request) {
        cartService.upsertItem(userId, request);
        return ResponseEntity.ok(ApiResponse.success(200, null));
    }

    @DeleteMapping("/cart/items/{variantId}")
    public ResponseEntity<Void> removeCartItem(
            @RequestParam @Positive Long userId,
            @PathVariable @Positive Long variantId) {
        cartService.removeItem(userId, variantId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cart/stores/{storeId}")
    public ResponseEntity<Void> clearStoreCart(
            @RequestParam @Positive Long userId,
            @PathVariable @Positive Long storeId) {
        cartService.removeStoreItems(userId, storeId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/customer")
    public ResponseEntity<ApiResponse<MarketplaceCustomerResponse>> upsertCustomer(
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody MarketplaceCustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(200, customerService.upsert(request)));
    }

    /** Used only to compensate a failed workspace registration. */
    @DeleteMapping("/customer")
    public ResponseEntity<Void> deleteFailedRegistration(
            @RequestParam @Positive Long userId,
            @RequestParam @Positive Long businessId) {
        customerService.deleteFailedRegistration(userId, businessId);
        return ResponseEntity.noContent().build();
    }
}
