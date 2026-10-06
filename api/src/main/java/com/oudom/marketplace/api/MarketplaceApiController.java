package com.oudom.marketplace.api;

import com.oudom.marketplace.MarketplaceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MarketplaceApiController {

    private final MarketplaceService service = new MarketplaceService();

    @Value("${payway.environment:sandbox}")
    private String paywayEnvironment;

    @Value("${payway.merchant-id:}")
    private String paywayMerchantId;

    @Value("${payway.api-key:}")
    private String paywayApiKey;

    @Value("${payway.checkout-url:https://checkout-sandbox.payway.com.kh/api/payment-gateway/v1/payments/purchase}")
    private String paywayCheckoutUrl;

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


    @GetMapping("/payway/config")
    public Map<String, Object> paywayConfig() {
        boolean configured = paywayMerchantId != null && !paywayMerchantId.isBlank()
            && paywayApiKey != null && !paywayApiKey.isBlank();
        return Map.of(
            "provider", "ABA PayWay",
            "environment", paywayEnvironment,
            "configured", configured,
            "checkoutUrl", configured ? paywayCheckoutUrl : "",
            "message", configured
                ? "ABA PayWay sandbox credentials are configured."
                : "Add PAYWAY_MERCHANT_ID and PAYWAY_API_KEY to enable real sandbox checkout."
        );
    }

    @PostMapping("/payway/checkout")
    public Map<String, Object> paywayCheckout(@RequestBody Map<String, Object> request) {
        boolean configured = paywayMerchantId != null && !paywayMerchantId.isBlank()
            && paywayApiKey != null && !paywayApiKey.isBlank();
        if (!configured) {
            return Map.of(
                "provider", "ABA PayWay",
                "environment", paywayEnvironment,
                "configured", false,
                "checkoutUrl", "",
                "message", "ABA PayWay sandbox credentials are not configured on the backend yet."
            );
        }

        return Map.of(
            "provider", "ABA PayWay",
            "environment", paywayEnvironment,
            "configured", true,
            "checkoutUrl", paywayCheckoutUrl,
            "message", "PayWay checkout endpoint is ready. Signed transaction fields are generated after sandbox credentials are supplied."
        );
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
