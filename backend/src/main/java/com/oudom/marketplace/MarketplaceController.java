package com.oudom.marketplace;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/marketplace")
@CrossOrigin(origins = "*")
public class MarketplaceController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "ai-asset-marketplace-api",
                "stack", "Java 21 + Spring Boot"
        );
    }

    @GetMapping("/seller/summary")
    public Map<String, Object> sellerSummary() {
        return Map.of(
                "publishedAssets", 12,
                "revenue", 1284,
                "orders", 96,
                "favorites", 418
        );
    }

    @GetMapping("/admin/summary")
    public Map<String, Object> adminSummary() {
        return Map.of(
                "assets", 8,
                "creators", 24,
                "ordersToday", 31,
                "gmvThisMonth", 8640,
                "approvalRate", 94,
                "refundRate", 1.8
        );
    }

    @PostMapping("/checkout")
    public Map<String, Object> checkout(@RequestBody CheckoutRequest request) {
        double total = request.items() == null ? 0 : request.items().stream()
                .mapToDouble(CartItem::price)
                .sum();

        return Map.of(
                "success", true,
                "message", "Demo checkout created",
                "itemCount", request.items() == null ? 0 : request.items().size(),
                "total", total
        );
    }

    @PostMapping("/seller/assets")
    public Map<String, Object> createAsset(@RequestBody AssetSubmission asset) {
        return Map.of(
                "success", true,
                "status", "PENDING_REVIEW",
                "title", asset.title(),
                "message", "Asset submitted for review"
        );
    }

    @PostMapping("/admin/assets/{id}/approve")
    public Map<String, Object> approve(@PathVariable Long id) {
        return Map.of("success", true, "assetId", id, "status", "PUBLISHED");
    }

    public record CartItem(Long id, String title, double price) {}
    public record CheckoutRequest(List<CartItem> items) {}
    public record AssetSubmission(String title, String category, double price, String description, String image) {}
}
