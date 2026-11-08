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
            new Asset(3L, "Dark Metrics Dashboard", "Data Analysis", "https://images.unsplash.com/photo-1556155092-490a1ba16284?auto=format&fit=crop&w=1200&q=80", 15.00, "MetricLab"),
            new Asset(4L, "Purple CRM Dashboard", "Project Management", "https://images.unsplash.com/photo-1543286386-713bdd548da4?auto=format&fit=crop&w=1200&q=80", 20.00, "AsterUI"),
            new Asset(5L, "Sales Overview Template", "Business Growth", "https://images.unsplash.com/photo-1531297484001-80022131f5a1?auto=format&fit=crop&w=1200&q=80", 9.00, "DashWorks"),
            new Asset(6L, "AI Operations Console", "AI", "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80", 25.00, "AgenticLab"),
            new Asset(7L, "Marketing Insight Board", "Digital Marketing", "https://images.unsplash.com/photo-1553484771-371a605b060b?auto=format&fit=crop&w=1200&q=80", 14.00, "OrbitStudio"),
            new Asset(8L, "Modern Admin Template", "Dashboard", "https://images.unsplash.com/photo-1558655146-9f40138edfeb?auto=format&fit=crop&w=1200&q=80", 17.00, "FrameLab")
        );
    }

    public Asset assetById(Long id) {
        if (id == null) return null;
        return assets().stream().filter(a -> a.id().equals(id)).findFirst().orElse(null);
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
