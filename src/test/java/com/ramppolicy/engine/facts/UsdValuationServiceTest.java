package com.ramppolicy.engine.facts;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UsdValuationServiceTest {

    @Test
    void valuesCryptoAmountsFromReferenceRatesWithDecimalMath() throws Exception {
        DemoFacts facts = DemoFacts.load(Path.of("src/main/resources/demo-data"), new ObjectMapper());
        UsdValuationService service = new UsdValuationService(facts.referenceRates());

        BigDecimal usd = service.cryptoToUsd("BTC", new BigDecimal("0.02"));

        assertEquals(0, new BigDecimal("1340.00").compareTo(usd));
    }
}
