package com.oudom.marketplace.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oudom.marketplace.MarketplaceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PayWayService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder().build();
    private final MarketplaceService marketplace = new MarketplaceService();
    private final PaymentOrderRepository orders;

    @Value("${payway.environment:sandbox}")
    private String environment;
    @Value("${payway.merchant-id:}")
    private String merchantId;
    @Value("${payway.api-key:}")
    private String apiKey;
    @Value("${payway.qr-url:https://checkout-sandbox.payway.com.kh/api/payment-gateway/v1/payments/generate-qr}")
    private String qrUrl;
    @Value("${payway.check-url:https://checkout-sandbox.payway.com.kh/api/payment-gateway/v1/payments/check-transaction-2}")
    private String checkUrl;
    @Value("${payway.callback-url:https://ai-asset-marketplace-api.onrender.com/api/payway/callback}")
    private String callbackUrl;

    public PayWayService(PaymentOrderRepository orders) {
        this.orders = orders;
    }

    public boolean configured() {
        return merchantId != null && !merchantId.isBlank() && apiKey != null && !apiKey.isBlank();
    }

    public Map<String, Object> config() {
        return Map.of(
            "provider", "ABA PayWay",
            "environment", environment,
            "configured", configured(),
            "databaseConfigured", orders.configured(),
            "message", configured() && orders.configured()
                ? "ABA PayWay and payment persistence are configured."
                : "PayWay credentials and DATABASE_URL must be configured on the backend."
        );
    }

    public Map<String, Object> initiate(Map<String, Object> request) throws Exception {
        if (!configured()) return Map.of("configured", false, "message", "ABA PayWay credentials are not configured.");
        if (!orders.configured()) return Map.of("configured", false, "message", "Payment database is not configured.");

        Map<String,Object> customer = castMap(request.get("customer"));
        String buyerEmail = String.valueOf(customer.getOrDefault("email", "")).trim().toLowerCase(Locale.ROOT);
        if (buyerEmail.isBlank() || !buyerEmail.contains("@")) throw new IllegalArgumentException("A valid buyer email is required.");

        List<MarketplaceService.CartItem> items = authoritativeItems(request.get("items"));
        if (items.isEmpty()) throw new IllegalArgumentException("No valid marketplace items were supplied.");

        String currency = String.valueOf(request.getOrDefault("currency", "USD")).toUpperCase(Locale.ROOT);
        if (!Set.of("USD", "KHR").contains(currency)) throw new IllegalArgumentException("Unsupported currency.");

        PaymentOrderRepository.PaymentOrder order = orders.createOrder(buyerEmail, currency, items);
        String tranId = newTransactionId();
        orders.bindTransaction(order.orderId(), tranId);
        order = orders.findByOrderId(order.orderId()).orElseThrow();

        return generateQr(order, items, customer);
    }

    public Map<String, Object> checkStatus(String tranId) throws Exception {
        PaymentOrderRepository.PaymentOrder order = orders.findByTranId(tranId)
            .orElseThrow(() -> new IllegalArgumentException("Unknown transaction."));
        if ("PAID".equals(order.status())) return safeStatus(order, true);
        if (!configured()) return safeStatus(order, false);

        String reqTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String hash = hmacBase64(reqTime + merchantId + tranId, apiKey);
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("req_time", reqTime);
        body.put("merchant_id", merchantId);
        body.put("tran_id", tranId);
        body.put("hash", hash);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(checkUrl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
            .build();
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) throw new IllegalStateException("PayWay status check returned HTTP " + response.statusCode());

        Map<String,Object> parsed = objectMapper.readValue(response.body(), new TypeReference<>() {});
        Map<String,Object> data = castMap(parsed.get("data"));
        boolean paid = PaymentVerifier.approvedAndMatches(order, data);

        String providerStatus = String.valueOf(data.getOrDefault("payment_status", "PENDING")).toUpperCase(Locale.ROOT);
        String nextStatus = paid ? "PAID"
            : providerStatus.contains("CANCEL") ? "CANCELLED"
            : (providerStatus.contains("FAIL") || providerStatus.contains("DECLIN")) ? "FAILED"
            : "PENDING";
        String bankRef = String.valueOf(data.getOrDefault("bank_ref", ""));
        orders.updateVerifiedStatus(tranId, nextStatus, bankRef, paid);

        PaymentOrderRepository.PaymentOrder updated = orders.findByTranId(tranId).orElseThrow();
        return safeStatus(updated, paid);
    }

    public Map<String,Object> acceptCallback(Map<String,Object> body) throws Exception {
        String tranId = String.valueOf(body.getOrDefault("tran_id", body.getOrDefault("transaction_id", ""))).trim();
        if (tranId.isBlank()) throw new IllegalArgumentException("Missing transaction id.");
        PaymentOrderRepository.PaymentOrder order = orders.findByTranId(tranId)
            .orElseThrow(() -> new IllegalArgumentException("Unknown transaction."));
        orders.recordCallback(tranId);
        // Callback data is notification only. Never trust it to mark an order PAID.
        return checkStatus(order.paywayTranId());
    }

    public Map<String,Object> orderStatus(String orderId, String buyerEmail) throws Exception {
        PaymentOrderRepository.PaymentOrder order = requireBuyer(orderId, buyerEmail);
        return safeStatus(order, "PAID".equals(order.status()));
    }

    public Map<String,Object> access(String orderId, String buyerEmail) throws Exception {
        PaymentOrderRepository.PaymentOrder order = requireBuyer(orderId, buyerEmail);
        boolean paid = "PAID".equals(order.status()) && order.verifiedAt() != null;
        if (!paid) return Map.of("orderId", orderId, "unlocked", false, "status", order.status());
        List<Map<String,Object>> assets = orders.items(orderId).stream()
            .map(i -> Map.<String,Object>of("assetId", i.assetId(), "title", i.title()))
            .collect(Collectors.toList());
        return Map.of("orderId", orderId, "unlocked", true, "status", "PAID", "assets", assets);
    }

    private Map<String,Object> generateQr(
        PaymentOrderRepository.PaymentOrder order,
        List<MarketplaceService.CartItem> items,
        Map<String,Object> customer
    ) throws Exception {
        String reqTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String name = String.valueOf(customer.getOrDefault("name", "")).trim();
        String firstName = name.isBlank() ? "Customer" : name.split("\\s+", 2)[0];
        String lastName = name.contains(" ") ? name.substring(name.indexOf(' ') + 1) : "";
        String email = order.buyerEmail();
        String phone = String.valueOf(customer.getOrDefault("phone", ""));

        List<Map<String,Object>> paywayItems = items.stream().map(item -> {
            Map<String,Object> row = new LinkedHashMap<>();
            row.put("name", item.title());
            row.put("quantity", 1);
            row.put("price", BigDecimal.valueOf(item.price()).setScale(2));
            return row;
        }).toList();
        String itemsBase64 = Base64.getEncoder().encodeToString(
            objectMapper.writeValueAsString(paywayItems).getBytes(StandardCharsets.UTF_8));
        String callback = Base64.getEncoder().encodeToString(callbackUrl.getBytes(StandardCharsets.UTF_8));

        LinkedHashMap<String,Object> body = new LinkedHashMap<>();
        body.put("req_time", reqTime);
        body.put("merchant_id", merchantId);
        body.put("tran_id", order.paywayTranId());
        body.put("first_name", firstName);
        body.put("last_name", lastName);
        body.put("email", email);
        body.put("phone", phone);
        body.put("amount", order.amount());
        body.put("purchase_type", "purchase");
        body.put("payment_option", "abapay_khqr");
        body.put("items", itemsBase64);
        body.put("currency", order.currency());
        body.put("callback_url", callback);
        body.put("return_deeplink", "");
        body.put("custom_fields", "");
        body.put("return_params", order.orderId());
        body.put("payout", "");
        body.put("lifetime", 6);
        body.put("qr_image_template", "template3_color");

        StringBuilder toSign = new StringBuilder();
        for (Object value : body.values()) toSign.append(value == null ? "" : value);
        body.put("hash", hmacBase64(toSign.toString(), apiKey));

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(qrUrl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
            .build();
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        Map<String,Object> parsed = objectMapper.readValue(response.body(), new TypeReference<>() {});
        if (response.statusCode() >= 400) {
            orders.updateVerifiedStatus(order.paywayTranId(), "FAILED", null, false);
            return Map.of("configured", true, "success", false, "orderId", order.orderId(),
                "tranId", order.paywayTranId(), "message", "ABA PayWay QR request failed.");
        }

        Object qrImage = parsed.get("qrImage");
        Object qrString = parsed.get("qrString");
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("configured", true);
        result.put("environment", environment);
        result.put("success", qrImage != null || qrString != null);
        result.put("orderId", order.orderId());
        result.put("tranId", order.paywayTranId());
        result.put("qrImage", qrImage == null ? "" : qrImage);
        result.put("qrString", qrString == null ? "" : qrString);
        result.put("abapayDeeplink", parsed.getOrDefault("abapay_deeplink", ""));
        result.put("amount", order.amount());
        result.put("currency", order.currency());
        result.put("paymentStatus", "PENDING");
        return result;
    }

    private List<MarketplaceService.CartItem> authoritativeItems(Object rawItems) {
        if (!(rawItems instanceof List<?> list)) return List.of();
        LinkedHashMap<Long,MarketplaceService.CartItem> result = new LinkedHashMap<>();
        for (Object raw : list) {
            Map<String,Object> item = castMap(raw);
            Long id;
            try { id = Long.valueOf(String.valueOf(item.get("id"))); } catch (Exception e) { continue; }
            MarketplaceService.Asset asset = marketplace.assetById(id);
            if (asset != null) result.put(id, new MarketplaceService.CartItem(asset.id(), asset.title(), asset.price()));
        }
        return new ArrayList<>(result.values());
    }

    private PaymentOrderRepository.PaymentOrder requireBuyer(String orderId, String buyerEmail) throws Exception {
        PaymentOrderRepository.PaymentOrder order = orders.findByOrderId(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Unknown order."));
        String email = buyerEmail == null ? "" : buyerEmail.trim().toLowerCase(Locale.ROOT);
        if (!order.buyerEmail().equals(email)) throw new SecurityException("Order does not belong to this buyer.");
        return order;
    }

    private Map<String,Object> safeStatus(PaymentOrderRepository.PaymentOrder order, boolean paid) {
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("orderId", order.orderId());
        result.put("tranId", order.paywayTranId());
        result.put("paymentStatus", order.status());
        result.put("paid", paid && "PAID".equals(order.status()) && order.verifiedAt() != null);
        result.put("unlocked", "PAID".equals(order.status()) && order.verifiedAt() != null);
        result.put("amount", order.amount());
        result.put("currency", order.currency());
        result.put("updatedAt", order.updatedAt().toString());
        return result;
    }

    private String newTransactionId() {
        return Long.toUnsignedString(UUID.randomUUID().getMostSignificantBits()).substring(0, 18);
    }

    private String hmacBase64(String value, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        return Base64.getEncoder().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }

    @SuppressWarnings("unchecked")
    private Map<String,Object> castMap(Object value) {
        return value instanceof Map<?,?> map ? (Map<String,Object>) map : Map.of();
    }
}
