package com.oudom.marketplace.api;

import com.oudom.marketplace.MarketplaceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MarketplaceApiController {

    private final MarketplaceService service = new MarketplaceService();

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "service", "marketplace-api", "stack", "Java 21 + Spring Boot");
    }

    @GetMapping("/assets")
    public List<MarketplaceService.Asset> assets() {
        return service.assets();
    }

    @GetMapping("/seller/summary")
    public Map<String, Object> sellerSummary() {
        return service.sellerSummary();
    }

    @GetMapping("/admin/summary")
    public Map<String, Object> adminSummary() {
        return service.adminSummary();
    }

    @PostMapping("/checkout")
    public Map<String, Object> checkout(@RequestBody MarketplaceService.CheckoutRequest request) {
        return service.checkout(request);
    }

    @PostMapping("/seller/assets")
    public Map<String, Object> createAsset(@RequestBody MarketplaceService.AssetSubmission asset) {
        return service.submitAsset(asset);
    }

    @PostMapping("/admin/assets/{id}/approve")
    public Map<String, Object> approve(@PathVariable Long id) {
        return service.approve(id);
    }
}
