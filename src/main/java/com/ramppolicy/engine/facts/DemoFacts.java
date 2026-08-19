package com.ramppolicy.engine.facts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ramppolicy.engine.domain.AddressRiskRecord;
import com.ramppolicy.engine.domain.AssetNetworkRecord;
import com.ramppolicy.engine.domain.CustomerRecord;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Authoritative demo facts loaded from vendored JSON resources.
 *
 * @param customers customer facts by customer id
 * @param assets asset/network facts by `asset/network`
 * @param addressRisks address risk facts by address
 * @param referenceRates reference rates by pair such as `BTC/USD`
 */
public record DemoFacts(
        Map<String, CustomerRecord> customers,
        Map<String, AssetNetworkRecord> assets,
        Map<String, AddressRiskRecord> addressRisks,
        Map<String, BigDecimal> referenceRates) {

    /**
     * Loads the vendored demo facts from a filesystem directory.
     *
     * @param baseDir filesystem directory containing the JSON inputs
     * @param objectMapper JSON parser
     * @return demo facts bundle
     * @throws IOException when a file is missing or malformed
     */
    public static DemoFacts load(Path baseDir, ObjectMapper objectMapper) throws IOException {
        return new DemoFacts(
                readCustomers(baseDir.resolve("customers.json"), objectMapper),
                readAssets(baseDir.resolve("assets.json"), objectMapper),
                readAddressRisks(baseDir.resolve("address_risk.json"), objectMapper),
                readReferenceRates(baseDir.resolve("reference_rates.json"), objectMapper));
    }

    /**
     * Loads the vendored demo facts from the classpath.
     *
     * @param resourceDir classpath directory such as {@code demo-data}
     * @param objectMapper JSON parser
     * @return demo facts bundle
     * @throws IOException when a resource is missing or malformed
     */
    public static DemoFacts loadFromClasspath(Path resourceDir, ObjectMapper objectMapper) throws IOException {
        String prefix = resourceDir.toString().replace('\\', '/');
        return new DemoFacts(
                readCustomers(prefix + "/customers.json", objectMapper),
                readAssets(prefix + "/assets.json", objectMapper),
                readAddressRisks(prefix + "/address_risk.json", objectMapper),
                readReferenceRates(prefix + "/reference_rates.json", objectMapper));
    }

    private static Map<String, CustomerRecord> readCustomers(Path path, ObjectMapper objectMapper) throws IOException {
        JsonNode root = objectMapper.readTree(Files.readString(path));
        Map<String, CustomerRecord> customers = new LinkedHashMap<>();
        root.fields().forEachRemaining(entry -> {
            JsonNode node = entry.getValue();
            customers.put(entry.getKey(), new CustomerRecord(
                    entry.getKey(),
                    node.path("name").asText(),
                    node.path("kyc_tier").asInt(),
                    node.path("monthly_limit_usd").decimalValue(),
                    node.path("verified_bank_name").asText(),
                    node.path("status").asText()));
        });
        return Map.copyOf(customers);
    }

    private static Map<String, AssetNetworkRecord> readAssets(Path path, ObjectMapper objectMapper) throws IOException {
        JsonNode root = objectMapper.readTree(Files.readString(path));
        Map<String, AssetNetworkRecord> assets = new LinkedHashMap<>();
        root.fields().forEachRemaining(assetEntry -> assetEntry.getValue().fields().forEachRemaining(networkEntry -> {
            JsonNode node = networkEntry.getValue();
            String asset = assetEntry.getKey();
            String network = networkEntry.getKey();
            assets.put(assetKey(asset, network), new AssetNetworkRecord(
                    asset,
                    network,
                    node.path("min_amount").decimalValue(),
                    node.path("confirmations_required").asInt()));
        }));
        return Map.copyOf(assets);
    }

    private static Map<String, AddressRiskRecord> readAddressRisks(Path path, ObjectMapper objectMapper) throws IOException {
        JsonNode root = objectMapper.readTree(Files.readString(path));
        Map<String, AddressRiskRecord> risks = new LinkedHashMap<>();
        root.fields().forEachRemaining(entry -> risks.put(entry.getKey(), new AddressRiskRecord(
                entry.getValue().path("risk_score").asInt(),
                entry.getValue().path("category").asText())));
        return Map.copyOf(risks);
    }

    private static Map<String, BigDecimal> readReferenceRates(Path path, ObjectMapper objectMapper) throws IOException {
        JsonNode root = objectMapper.readTree(Files.readString(path));
        Map<String, BigDecimal> rates = new LinkedHashMap<>();
        root.fields().forEachRemaining(entry -> rates.put(entry.getKey(), entry.getValue().decimalValue()));
        return Map.copyOf(rates);
    }

    private static Map<String, CustomerRecord> readCustomers(String resource, ObjectMapper objectMapper) throws IOException {
        JsonNode root = readResourceTree(resource, objectMapper);
        Map<String, CustomerRecord> customers = new LinkedHashMap<>();
        root.fields().forEachRemaining(entry -> {
            JsonNode node = entry.getValue();
            customers.put(entry.getKey(), new CustomerRecord(
                    entry.getKey(),
                    node.path("name").asText(),
                    node.path("kyc_tier").asInt(),
                    node.path("monthly_limit_usd").decimalValue(),
                    node.path("verified_bank_name").asText(),
                    node.path("status").asText()));
        });
        return Map.copyOf(customers);
    }

    private static Map<String, AssetNetworkRecord> readAssets(String resource, ObjectMapper objectMapper) throws IOException {
        JsonNode root = readResourceTree(resource, objectMapper);
        Map<String, AssetNetworkRecord> assets = new LinkedHashMap<>();
        root.fields().forEachRemaining(assetEntry -> assetEntry.getValue().fields().forEachRemaining(networkEntry -> {
            JsonNode node = networkEntry.getValue();
            String asset = assetEntry.getKey();
            String network = networkEntry.getKey();
            assets.put(assetKey(asset, network), new AssetNetworkRecord(
                    asset,
                    network,
                    node.path("min_amount").decimalValue(),
                    node.path("confirmations_required").asInt()));
        }));
        return Map.copyOf(assets);
    }

    private static Map<String, AddressRiskRecord> readAddressRisks(String resource, ObjectMapper objectMapper) throws IOException {
        JsonNode root = readResourceTree(resource, objectMapper);
        Map<String, AddressRiskRecord> risks = new LinkedHashMap<>();
        root.fields().forEachRemaining(entry -> risks.put(entry.getKey(), new AddressRiskRecord(
                entry.getValue().path("risk_score").asInt(),
                entry.getValue().path("category").asText())));
        return Map.copyOf(risks);
    }

    private static Map<String, BigDecimal> readReferenceRates(String resource, ObjectMapper objectMapper) throws IOException {
        JsonNode root = readResourceTree(resource, objectMapper);
        Map<String, BigDecimal> rates = new LinkedHashMap<>();
        root.fields().forEachRemaining(entry -> rates.put(entry.getKey(), entry.getValue().decimalValue()));
        return Map.copyOf(rates);
    }

    private static JsonNode readResourceTree(String resource, ObjectMapper objectMapper) throws IOException {
        ClassLoader classLoader = DemoFacts.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(resource)) {
            if (inputStream == null) {
                throw new IOException("Missing classpath resource: " + resource);
            }
            return objectMapper.readTree(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    /**
     * Looks up the asset/network configuration for a declared transfer.
     *
     * @param asset asset ticker
     * @param network network code
     * @return asset/network record or null when unsupported
     */
    public AssetNetworkRecord asset(String asset, String network) {
        return assets.get(assetKey(asset, network));
    }

    private static String assetKey(String asset, String network) {
        return asset + "/" + network;
    }
}
