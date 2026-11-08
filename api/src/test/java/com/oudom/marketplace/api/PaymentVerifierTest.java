package com.oudom.marketplace.api;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PaymentVerifierTest {

    private PaymentOrderRepository.PaymentOrder order() {
        return new PaymentOrderRepository.PaymentOrder(
            "ORD-1","buyer@example.com",new BigDecimal("12.00"),"USD","PENDING",
            "123456789",null, Instant.now(), Instant.now(), null,0
        );
    }

    @Test
    void approvesOnlyMatchingSuccessfulTransaction() {
        assertTrue(PaymentVerifier.approvedAndMatches(order(), Map.of(
            "payment_status_code",0,
            "payment_status","APPROVED",
            "original_amount",12.00,
            "original_currency","USD"
        )));
    }

    @Test
    void rejectsAmountMismatch() {
        assertFalse(PaymentVerifier.approvedAndMatches(order(), Map.of(
            "payment_status_code",0,
            "payment_status","APPROVED",
            "original_amount",1.00,
            "original_currency","USD"
        )));
    }

    @Test
    void rejectsCurrencyMismatch() {
        assertFalse(PaymentVerifier.approvedAndMatches(order(), Map.of(
            "payment_status_code",0,
            "payment_status","APPROVED",
            "original_amount",12.00,
            "original_currency","KHR"
        )));
    }

    @Test
    void rejectsPendingTransaction() {
        assertFalse(PaymentVerifier.approvedAndMatches(order(), Map.of(
            "payment_status_code",2,
            "payment_status","PENDING",
            "original_amount",12.00,
            "original_currency","USD"
        )));
    }
}
