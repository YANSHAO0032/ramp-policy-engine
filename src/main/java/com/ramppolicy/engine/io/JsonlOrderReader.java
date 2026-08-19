package com.ramppolicy.engine.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ramppolicy.engine.domain.CounterpartyRecord;
import com.ramppolicy.engine.domain.DepositRecord;
import com.ramppolicy.engine.domain.OrderRecord;
import com.ramppolicy.engine.domain.OrderType;
import com.ramppolicy.engine.domain.PayoutRecord;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Line-safe JSONL reader for demo orders.
 */
public final class JsonlOrderReader {

    private final ObjectMapper objectMapper;

    public JsonlOrderReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Parses a JSONL document into order records.
     *
     * @param jsonl JSONL payload
     * @return parsed orders
     * @throws IOException when any line is malformed
     */
    public List<OrderRecord> readAll(String jsonl) throws IOException {
        List<OrderRecord> orders = new ArrayList<>();
        for (String line : jsonl.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            orders.add(readOne(trimmed));
        }
        return orders;
    }

    /**
     * Parses a single order record from JSON.
     *
     * @param json JSON payload
     * @return parsed order
     * @throws IOException when the payload is malformed
     */
    public OrderRecord readOne(String json) throws IOException {
        JsonNode node = objectMapper.readTree(json);
        OrderType type = parseOrderType(node.path("type").asText());
        return switch (type) {
            case ON_RAMP -> new OrderRecord(
                    requiredText(node, "order_id"),
                    type,
                    requiredText(node, "customer_id"),
                    requiredText(node, "asset"),
                    requiredText(node, "network"),
                    decimal(node.path("fiat_amount_usd")),
                    decimal(node.path("quoted_crypto_amount")),
                    Instant.parse(requiredText(node, "quote_expires_at")),
                    node.path("fiat_status").asText(null),
                    node.path("destination_address").asText(null),
                    null,
                    null,
                    null,
                    null,
                    node.path("customer_note").asText(null));
            case OFF_RAMP -> new OrderRecord(
                    requiredText(node, "order_id"),
                    type,
                    requiredText(node, "customer_id"),
                    requiredText(node, "asset"),
                    requiredText(node, "network"),
                    null,
                    decimal(node.path("quoted_crypto_amount")),
                    Instant.parse(requiredText(node, "quote_expires_at")),
                    null,
                    null,
                    new DepositRecord(
                            requiredText(node.path("deposit"), "tx_hash"),
                            requiredText(node.path("deposit"), "from_address"),
                            node.path("deposit").path("confirmations").isMissingNode() ? null : node.path("deposit").path("confirmations").asInt(),
                            decimal(node.path("deposit").path("observed_amount")),
                            node.path("deposit").path("network").asText(null)),
                    new PayoutRecord(
                            requiredText(node.path("payout"), "bank_account_name"),
                            requiredText(node.path("payout"), "currency"),
                            decimal(node.path("payout").path("amount"))),
                    null,
                    null,
                    node.path("customer_note").asText(null));
            case WITHDRAWAL -> new OrderRecord(
                    requiredText(node, "order_id"),
                    type,
                    requiredText(node, "customer_id"),
                    requiredText(node, "asset"),
                    requiredText(node, "network"),
                    null,
                    null,
                    null,
                    null,
                    requiredText(node, "destination_address"),
                    null,
                    null,
                    decimal(node.path("amount")),
                    new CounterpartyRecord(
                            node.path("counterparty").path("is_vasp").asBoolean(false),
                            node.path("counterparty").path("vasp_name").asText(null),
                            node.path("counterparty").path("beneficiary_info").isMissingNode() || node.path("counterparty").path("beneficiary_info").isNull()
                                    ? null
                                    : node.path("counterparty").path("beneficiary_info")),
                    node.path("customer_note").asText(null));
        };
    }

    private static BigDecimal decimal(JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull() ? null : node.decimalValue();
    }

    private static String requiredText(JsonNode node, String fieldName) {
        JsonNode field = node.path(fieldName);
        if (field.isMissingNode() || field.isNull()) {
            throw new IllegalArgumentException("Missing required field: " + fieldName);
        }
        return field.asText();
    }

    private static OrderType parseOrderType(String value) {
        return switch (value) {
            case "on_ramp" -> OrderType.ON_RAMP;
            case "off_ramp" -> OrderType.OFF_RAMP;
            case "withdrawal" -> OrderType.WITHDRAWAL;
            default -> throw new IllegalArgumentException("Unknown order type: " + value);
        };
    }
}
