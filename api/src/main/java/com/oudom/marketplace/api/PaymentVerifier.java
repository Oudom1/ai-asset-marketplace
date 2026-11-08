package com.oudom.marketplace.api;

import java.math.BigDecimal;
import java.util.Map;

final class PaymentVerifier {
    private PaymentVerifier() {}

    static boolean approvedAndMatches(PaymentOrderRepository.PaymentOrder order, Map<String,Object> data) {
        if (order == null || data == null || data.isEmpty()) return false;
        boolean approved = "0".equals(String.valueOf(data.get("payment_status_code")))
            && "APPROVED".equalsIgnoreCase(String.valueOf(data.get("payment_status")));
        if (!approved) return false;

        BigDecimal actualAmount;
        try {
            actualAmount = new BigDecimal(String.valueOf(data.get("original_amount"))).setScale(2);
        } catch (Exception e) {
            return false;
        }
        String actualCurrency = String.valueOf(data.getOrDefault("original_currency", ""));
        return order.amount().setScale(2).compareTo(actualAmount) == 0
            && order.currency().equalsIgnoreCase(actualCurrency);
    }
}
