package com.oudom.marketplace.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PayWayService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder().build();
    private final Map<String, String> localStatus = new ConcurrentHashMap<>();

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

    public boolean configured() {
        return merchantId != null && !merchantId.isBlank() && apiKey != null && !apiKey.isBlank();
    }

    public Map<String, Object> config() {
        return Map.of(
            "provider", "ABA PayWay",
            "environment", environment,
            "configured", configured(),
            "message", configured()
                ? "ABA PayWay sandbox credentials are configured."
                : "Add PAYWAY_MERCHANT_ID and PAYWAY_API_KEY to the backend environment."
        );
    }

    public Map<String, Object> generateQr(Map<String, Object> request) throws Exception {
        if (!configured()) {
            return Map.of(
                "configured", false,
                "environment", environment,
                "message", "ABA PayWay sandbox credentials are not configured."
            );
        }

        String reqTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String tranId = String.valueOf(System.currentTimeMillis());
        if (tranId.length() > 20) tranId = tranId.substring(tranId.length() - 20);

        BigDecimal amount = new BigDecimal(String.valueOf(request.getOrDefault("amount", "0")));
        String currency = String.valueOf(request.getOrDefault("currency", "USD"));
        Map<String, Object> customer = castMap(request.get("customer"));
        String name = String.valueOf(customer.getOrDefault("name", "")).trim();
        String firstName = name.isBlank() ? "Customer" : name.split("\\s+", 2)[0];
        String lastName = name.contains(" ") ? name.substring(name.indexOf(' ') + 1) : "";
        String email = String.valueOf(customer.getOrDefault("email", ""));
        String phone = String.valueOf(customer.getOrDefault("phone", ""));

        List<Map<String, Object>> paywayItems = new ArrayList<>();
        Object rawItems = request.get("items");
        if (rawItems instanceof List<?> list) {
            for (Object item : list) {
                Map<String, Object> src = castMap(item);
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("name", String.valueOf(src.getOrDefault("title", "Marketplace item")));
                row.put("quantity", 1);
                row.put("price", new BigDecimal(String.valueOf(src.getOrDefault("price", "0"))));
                paywayItems.add(row);
            }
        }
        String itemsJson = objectMapper.writeValueAsString(paywayItems);
        String items = Base64.getEncoder().encodeToString(itemsJson.getBytes(StandardCharsets.UTF_8));
        String callback = Base64.getEncoder().encodeToString(callbackUrl.getBytes(StandardCharsets.UTF_8));

        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("req_time", reqTime);
        body.put("merchant_id", merchantId);
        body.put("tran_id", tranId);
        body.put("first_name", firstName);
        body.put("last_name", lastName);
        body.put("email", email);
        body.put("phone", phone);
        body.put("amount", amount);
        body.put("purchase_type", "purchase");
        body.put("payment_option", "abapay_khqr");
        body.put("items", items);
        body.put("currency", currency);
        body.put("callback_url", callback);
        body.put("return_deeplink", "");
        body.put("custom_fields", "");
        body.put("return_params", "");
        body.put("payout", "");
        body.put("lifetime", 6);
        body.put("qr_image_template", "template3_color");

        StringBuilder toSign = new StringBuilder();
        for (Object value : body.values()) {
            toSign.append(value == null ? "" : value);
        }
        body.put("hash", hmacBase64(toSign.toString(), apiKey));

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(qrUrl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
            .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> parsed = objectMapper.readValue(response.body(), new TypeReference<>() {});

        if (response.statusCode() >= 400) {
            return Map.of(
                "configured", true,
                "environment", environment,
                "tranId", tranId,
                "success", false,
                "message", "ABA PayWay QR request failed.",
                "paywayResponse", parsed
            );
        }

        Object qrImage = parsed.get("qrImage");
        Object qrString = parsed.get("qrString");
        localStatus.put(tranId, "PENDING");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("configured", true);
        result.put("environment", environment);
        result.put("success", qrImage != null || qrString != null);
        result.put("tranId", tranId);
        result.put("qrImage", qrImage == null ? "" : qrImage);
        result.put("qrString", qrString == null ? "" : qrString);
        result.put("abapayDeeplink", parsed.getOrDefault("abapay_deeplink", ""));
        result.put("amount", parsed.getOrDefault("amount", amount));
        result.put("currency", parsed.getOrDefault("currency", currency));
        result.put("status", parsed.getOrDefault("status", Map.of()));
        return result;
    }

    public Map<String, Object> checkStatus(String tranId) throws Exception {
        String local = localStatus.getOrDefault(tranId, "PENDING");
        if (!configured() || "PAID".equals(local)) {
            return Map.of("tranId", tranId, "paymentStatus", local, "paid", "PAID".equals(local));
        }

        String reqTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String hash = hmacBase64(reqTime + merchantId + tranId, apiKey);

        Map<String, Object> body = new LinkedHashMap<>();
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
        Map<String, Object> parsed = objectMapper.readValue(response.body(), new TypeReference<>() {});
        Map<String, Object> data = castMap(parsed.get("data"));

        boolean paid = "0".equals(String.valueOf(data.get("payment_status_code")))
            || "APPROVED".equalsIgnoreCase(String.valueOf(data.get("payment_status")));
        String status = paid ? "PAID" : String.valueOf(data.getOrDefault("payment_status", local));
        if (paid) localStatus.put(tranId, "PAID");

        return Map.of(
            "tranId", tranId,
            "paymentStatus", status,
            "paid", paid,
            "payway", parsed
        );
    }

    public boolean verifyCallback(Map<String, Object> body, String receivedSignature) throws Exception {
        if (receivedSignature == null || receivedSignature.isBlank()) return false;
        TreeMap<String, Object> sorted = new TreeMap<>(body);
        StringBuilder values = new StringBuilder();
        for (Object value : sorted.values()) {
            if (value instanceof Map || value instanceof List) {
                values.append(objectMapper.writeValueAsString(value));
            } else {
                values.append(value == null ? "" : value);
            }
        }
        String expected = hmacBase64(values.toString(), apiKey);
        return constantTimeEquals(expected, receivedSignature);
    }

    public void acceptCallback(Map<String, Object> body) {
        String tranId = String.valueOf(body.getOrDefault("tran_id", ""));
        String status = String.valueOf(body.getOrDefault("status", ""));
        if (!tranId.isBlank()) {
            localStatus.put(tranId, "0".equals(status) ? "PAID" : "FAILED");
        }
    }

    private String hmacBase64(String value, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        return Base64.getEncoder().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }

    private boolean constantTimeEquals(String a, String b) {
        byte[] aa = a.getBytes(StandardCharsets.UTF_8);
        byte[] bb = b.getBytes(StandardCharsets.UTF_8);
        if (aa.length != bb.length) return false;
        int diff = 0;
        for (int i = 0; i < aa.length; i++) diff |= aa[i] ^ bb[i];
        return diff == 0;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }
}
