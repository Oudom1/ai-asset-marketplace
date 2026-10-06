package com.oudom.marketplace.api;

import com.oudom.marketplace.MarketplaceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MarketplaceApiController {

    private final MarketplaceService service = new MarketplaceService();
    private final PayWayService payWayService;

    public MarketplaceApiController(PayWayService payWayService) {
        this.payWayService = payWayService;
    }

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
        return payWayService.config();
    }

    @PostMapping("/payway/qr")
    public ResponseEntity<Map<String, Object>> generatePayWayQr(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> result = payWayService.generateQr(request);
            if (Boolean.FALSE.equals(result.get("configured"))) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(result);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "success", false,
                "message", "Unable to generate ABA PayWay QR.",
                "error", e.getMessage() == null ? "Unknown PayWay error" : e.getMessage()
            ));
        }
    }

    @GetMapping("/payway/status/{tranId}")
    public ResponseEntity<Map<String, Object>> payWayStatus(@PathVariable String tranId) {
        try {
            return ResponseEntity.ok(payWayService.checkStatus(tranId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "tranId", tranId,
                "paid", false,
                "paymentStatus", "PENDING",
                "message", "Unable to verify payment status right now."
            ));
        }
    }

    @PostMapping("/payway/callback")
    public ResponseEntity<Map<String, Object>> payWayCallback(
        @RequestBody Map<String, Object> body,
        @RequestHeader(value = "X-PayWay-HMAC-SHA512", required = false) String signature
    ) {
        try {
            if (!payWayService.verifyCallback(body, signature)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Invalid PayWay callback signature."
                ));
            }
            payWayService.acceptCallback(body);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Unable to process PayWay callback."
            ));
        }
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
