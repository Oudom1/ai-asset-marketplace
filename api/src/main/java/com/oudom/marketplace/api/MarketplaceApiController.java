package com.oudom.marketplace.api;

import com.oudom.marketplace.MarketplaceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MarketplaceApiController {

    private final MarketplaceService service = new MarketplaceService();
    private final PayWayService payWayService;

    public MarketplaceApiController(PayWayService payWayService) {
        this.payWayService = payWayService;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String,Object> payway = payWayService.config();
        return Map.of(
            "status", "UP",
            "service", "marketplace-api",
            "stack", "Java 21 + Spring Boot",
            "paywayConfigured", payway.get("configured"),
            "databaseConfigured", payway.get("databaseConfigured")
        );
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

    @PostMapping({"/payway/initiate", "/payway/qr"})
    public ResponseEntity<Map<String, Object>> initiatePayWay(@RequestBody Map<String, Object> request) {
        try {
            Map<String,Object> result = payWayService.initiate(request);
            if (Boolean.FALSE.equals(result.get("configured"))) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(result);
            }
            if (Boolean.FALSE.equals(result.get("success"))) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(result);
            }
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "success", false,
                "message", "Unable to initiate ABA PayWay payment."
            ));
        }
    }

    @GetMapping("/payway/status/{tranId}")
    public ResponseEntity<Map<String, Object>> payWayStatus(@PathVariable String tranId) {
        try {
            return ResponseEntity.ok(payWayService.checkStatus(tranId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "tranId", tranId, "paid", false, "paymentStatus", "UNKNOWN", "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "tranId", tranId, "paid", false, "paymentStatus", "PENDING",
                "message", "Unable to verify payment status right now."
            ));
        }
    }

    @PostMapping("/payway/callback")
    public ResponseEntity<Map<String, Object>> payWayCallback(@RequestBody Map<String, Object> body) {
        try {
            // Callback is notification only. PayWayService always verifies server-to-server before PAID.
            return ResponseEntity.ok(payWayService.acceptCallback(body));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "success", false, "message", "Payment notification received but verification could not be completed."
            ));
        }
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<Map<String,Object>> orderStatus(
        @PathVariable String orderId,
        @RequestHeader("X-Buyer-Email") String buyerEmail
    ) {
        try {
            return ResponseEntity.ok(payWayService.orderStatus(orderId, buyerEmail));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Order access denied."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Order not found."));
        }
    }

    @GetMapping("/orders/{orderId}/access")
    public ResponseEntity<Map<String,Object>> orderAccess(
        @PathVariable String orderId,
        @RequestHeader("X-Buyer-Email") String buyerEmail
    ) {
        try {
            return ResponseEntity.ok(payWayService.access(orderId, buyerEmail));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Order access denied."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Order not found."));
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
