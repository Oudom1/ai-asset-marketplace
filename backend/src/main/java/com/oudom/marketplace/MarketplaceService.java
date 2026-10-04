package com.oudom.marketplace;

import java.util.List;
import java.util.Map;

public class MarketplaceService {

    public record Asset(Long id, String title, String category, String image, double price, String creator) {}
    public record CartItem(Long id, String title, double price) {}
    public record CheckoutRequest(List<CartItem> items) {}
    public record AssetSubmission(String title, String category, double price, String description, String image) {}

    public List<Asset> assets() {
        return List.of(
            new Asset(1L, "Analytics Dashboard UI", "Dashboard", "https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=1200&q=80", 12.00, "PixelForge"),
            new Asset(2L, "Finance Admin Dashboard", "UI Design", "https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=1200&q=80", 18.00, "NovaStudio"),
            new Asset(3L, "Dark Metrics Dashboard", "Data Analysis", "https://images.unsplash.com/photo-1556155092-490a1ba16284?auto=format&fit=crop&w=1200&q=80", 15.00, "MetricLab")
        );
    }

    public Map<String, Object> sellerSummary() {
        return Map.of("publishedAssets", 12, "revenue", 1284, "orders", 96, "favorites", 418);
    }

    public Map<String, Object> adminSummary() {
        return Map.of("assets", 8, "creators", 24, "ordersToday", 31, "gmvThisMonth", 8640, "approvalRate", 94, "refundRate", 1.8);
    }

    public Map<String, Object> checkout(CheckoutRequest request) {
        double total = request == null || request.items() == null ? 0 : request.items().stream().mapToDouble(CartItem::price).sum();
        int count = request == null || request.items() == null ? 0 : request.items().size();
        return Map.of("success", true, "message", "Demo checkout created", "itemCount", count, "total", total);
    }

    public Map<String, Object> submitAsset(AssetSubmission asset) {
        return Map.of("success", true, "status", "PENDING_REVIEW", "title", asset.title(), "message", "Asset submitted for review");
    }

    public Map<String, Object> approve(Long id) {
        return Map.of("success", true, "assetId", id, "status", "PUBLISHED");
    }
}
