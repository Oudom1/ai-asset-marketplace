package com.oudom.marketplace.api;

import com.oudom.marketplace.MarketplaceService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PaymentOrderRepository {
    public record PaymentOrder(
        String orderId, String buyerEmail, BigDecimal amount, String currency, String status,
        String paywayTranId, String bankRef, Instant createdAt, Instant updatedAt, Instant verifiedAt,
        int callbackCount
    ) {}
    public record OrderItem(long assetId, String title, BigDecimal unitPrice) {}

    private final String databaseUrl;

    public PaymentOrderRepository(@Value("${database.url:}") String databaseUrl) {
        this.databaseUrl = normalize(databaseUrl);
    }

    public boolean configured() {
        return databaseUrl != null && !databaseUrl.isBlank();
    }

    @PostConstruct
    void verifySchema() throws SQLException {
        if (!configured()) return;
        try (Connection c = open(); Statement s = c.createStatement()) {
            s.execute("SELECT 1 FROM payment_orders LIMIT 1");
            s.execute("SELECT 1 FROM payment_order_items LIMIT 1");
        }
    }

    public PaymentOrder createOrder(String buyerEmail, String currency, List<MarketplaceService.CartItem> items) throws SQLException {
        if (!configured()) throw new IllegalStateException("Payment database is not configured");
        if (buyerEmail == null || buyerEmail.isBlank()) throw new IllegalArgumentException("Buyer email is required");
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("At least one item is required");

        BigDecimal amount = items.stream()
            .map(i -> BigDecimal.valueOf(i.price()))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2);
        String orderId = "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase(Locale.ROOT);
        String normalizedCurrency = currency == null || currency.isBlank() ? "USD" : currency.toUpperCase(Locale.ROOT);

        try (Connection c = open()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO payment_orders(order_id,buyer_email,amount,currency,status) VALUES(?,?,?,?, 'PENDING')")) {
                ps.setString(1, orderId);
                ps.setString(2, buyerEmail.trim().toLowerCase(Locale.ROOT));
                ps.setBigDecimal(3, amount);
                ps.setString(4, normalizedCurrency);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO payment_order_items(order_id,asset_id,title,unit_price) VALUES(?,?,?,?)")) {
                for (MarketplaceService.CartItem item : items) {
                    ps.setString(1, orderId);
                    ps.setLong(2, item.id());
                    ps.setString(3, item.title());
                    ps.setBigDecimal(4, BigDecimal.valueOf(item.price()).setScale(2));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            c.commit();
        }
        return findByOrderId(orderId).orElseThrow();
    }

    public void bindTransaction(String orderId, String tranId) throws SQLException {
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(
            "UPDATE payment_orders SET payway_tran_id=?,updated_at=now() WHERE order_id=? AND payway_tran_id IS NULL")) {
            ps.setString(1, tranId);
            ps.setString(2, orderId);
            if (ps.executeUpdate() != 1) throw new IllegalStateException("Order transaction already bound or order missing");
        }
    }

    public Optional<PaymentOrder> findByOrderId(String orderId) throws SQLException {
        return find("SELECT * FROM payment_orders WHERE order_id=?", orderId);
    }

    public Optional<PaymentOrder> findByTranId(String tranId) throws SQLException {
        return find("SELECT * FROM payment_orders WHERE payway_tran_id=?", tranId);
    }

    public List<OrderItem> items(String orderId) throws SQLException {
        List<OrderItem> result = new ArrayList<>();
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(
            "SELECT asset_id,title,unit_price FROM payment_order_items WHERE order_id=? ORDER BY asset_id")) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(new OrderItem(rs.getLong(1), rs.getString(2), rs.getBigDecimal(3)));
            }
        }
        return result;
    }

    public void recordCallback(String tranId) throws SQLException {
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(
            "UPDATE payment_orders SET callback_count=callback_count+1,updated_at=now() WHERE payway_tran_id=?")) {
            ps.setString(1, tranId);
            ps.executeUpdate();
        }
    }

    public void updateVerifiedStatus(String tranId, String status, String bankRef, boolean verifiedPaid) throws SQLException {
        String normalized = status == null || status.isBlank() ? "PENDING" : status.toUpperCase(Locale.ROOT);
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(
            "UPDATE payment_orders SET status=?,bank_ref=COALESCE(?,bank_ref),updated_at=now(),verified_at=CASE WHEN ? THEN now() ELSE verified_at END WHERE payway_tran_id=?")) {
            ps.setString(1, normalized);
            ps.setString(2, bankRef == null || bankRef.isBlank() ? null : bankRef);
            ps.setBoolean(3, verifiedPaid);
            ps.setString(4, tranId);
            ps.executeUpdate();
        }
    }

    private Optional<PaymentOrder> find(String sql, String value) throws SQLException {
        if (!configured()) return Optional.empty();
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    private PaymentOrder map(ResultSet rs) throws SQLException {
        Timestamp verified = rs.getTimestamp("verified_at");
        return new PaymentOrder(
            rs.getString("order_id"), rs.getString("buyer_email"), rs.getBigDecimal("amount"),
            rs.getString("currency"), rs.getString("status"), rs.getString("payway_tran_id"),
            rs.getString("bank_ref"), rs.getTimestamp("created_at").toInstant(),
            rs.getTimestamp("updated_at").toInstant(), verified == null ? null : verified.toInstant(),
            rs.getInt("callback_count")
        );
    }

    private Connection open() throws SQLException {
        return DriverManager.getConnection(databaseUrl);
    }

    private static String normalize(String url) {
        if (url == null) return "";
        if (url.startsWith("postgresql://")) return "jdbc:" + url;
        if (url.startsWith("postgres://")) return "jdbc:postgresql://" + url.substring("postgres://".length());
        return url;
    }
}
