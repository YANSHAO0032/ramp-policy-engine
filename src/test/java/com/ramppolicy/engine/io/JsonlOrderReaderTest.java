package com.ramppolicy.engine.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ramppolicy.engine.domain.OrderRecord;
import com.ramppolicy.engine.domain.OrderType;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonlOrderReaderTest {

    @Test
    void readsAllDemoOrders() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonlOrderReader reader = new JsonlOrderReader(objectMapper);
        String jsonl = Files.readString(Path.of("src/test/resources/demo-data/orders.jsonl"));

        List<OrderRecord> orders = reader.readAll(jsonl);

        assertEquals(14, orders.size());
        assertEquals("O-001", orders.get(0).orderId());
        assertEquals(OrderType.ON_RAMP, orders.get(0).type());
        assertEquals("O-014", orders.get(13).orderId());
        assertNotNull(orders.get(1).deposit());
    }

    @Test
    void rejectsMissingRequiredFields() {
        JsonlOrderReader reader = new JsonlOrderReader(new ObjectMapper());

        assertThrows(IllegalArgumentException.class, () -> reader.readOne("""
                {"order_id":"X-1","type":"on_ramp","customer_id":"c001","asset":"USDT","quote_expires_at":"2026-07-28T12:05:00Z"}
                """));
    }
}
