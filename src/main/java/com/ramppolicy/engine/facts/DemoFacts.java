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
 * 从仓库内置 JSON 资源加载的 Demo 权威事实集合。
 *
 * @param customers 按客户标识索引的客户事实
 * @param assets 按 asset/network 索引的资产网络事实
 * @param addressRisks 按地址索引的风险事实
 * @param referenceRates 按 BTC/USD 等交易对索引的参考汇率
 */
public record DemoFacts(
        Map<String, CustomerRecord> customers,
        Map<String, AssetNetworkRecord> assets,
        Map<String, AddressRiskRecord> addressRisks,
        Map<String, BigDecimal> referenceRates) {

    /**
     * 从文件系统目录加载仓库内置 Demo 事实。
     *
     * @param baseDir 包含 JSON 输入文件的目录
     * @param objectMapper JSON 解析器
     * @return Demo 事实集合
     * @throws IOException 文件缺失或格式错误时抛出
     */
    public static DemoFacts load(Path baseDir, ObjectMapper objectMapper) throws IOException {
        return new DemoFacts(
                readCustomers(baseDir.resolve("customers.json"), objectMapper),
                readAssets(baseDir.resolve("assets.json"), objectMapper),
                readAddressRisks(baseDir.resolve("address_risk.json"), objectMapper),
                readReferenceRates(baseDir.resolve("reference_rates.json"), objectMapper));
    }

    /**
     * 从类路径加载仓库内置 Demo 事实。
     *
     * @param resourceDir 类路径目录，例如 {@code demo-data}
     * @param objectMapper JSON 解析器
     * @return Demo 事实集合
     * @throws IOException 资源缺失或格式错误时抛出
     */
    public static DemoFacts loadFromClasspath(Path resourceDir, ObjectMapper objectMapper) throws IOException {
        String prefix = resourceDir.toString().replace('\\', '/');
        return new DemoFacts(
                readCustomers(prefix + "/customers.json", objectMapper),
                readAssets(prefix + "/assets.json", objectMapper),
                readAddressRisks(prefix + "/address_risk.json", objectMapper),
                readReferenceRates(prefix + "/reference_rates.json", objectMapper));
    }

    /**
     * 从文件系统中的客户文件读取客户事实。
     *
     * @param path 客户文件路径
     * @param objectMapper JSON 解析器
     * @return 按客户标识索引的客户事实
     * @throws IOException 文件缺失或格式错误时抛出
     */
    /**
     * 从文件系统中的客户文件读取客户事实。
     *
     * @param path 客户文件路径
     * @param objectMapper JSON 解析器
     * @return 按客户标识索引的客户事实
     * @throws IOException 文件缺失或格式错误时抛出
     */
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

    /**
     * 从文件系统中的资产文件读取资产网络事实。
     *
     * @param path 资产文件路径
     * @param objectMapper JSON 解析器
     * @return 按 asset/network 索引的资产网络事实
     * @throws IOException 文件缺失或格式错误时抛出
     */
    /**
     * 从文件系统中的资产文件读取资产网络事实。
     *
     * @param path 资产文件路径
     * @param objectMapper JSON 解析器
     * @return 按 asset/network 索引的资产网络事实
     * @throws IOException 文件缺失或格式错误时抛出
     */
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

    /**
     * 从文件系统中的地址风险文件读取风险事实。
     *
     * @param path 风险文件路径
     * @param objectMapper JSON 解析器
     * @return 按地址索引的风险事实
     * @throws IOException 文件缺失或格式错误时抛出
     */
    /**
     * 从文件系统中的地址风险文件读取风险事实。
     *
     * @param path 风险文件路径
     * @param objectMapper JSON 解析器
     * @return 按地址索引的风险事实
     * @throws IOException 文件缺失或格式错误时抛出
     */
    private static Map<String, AddressRiskRecord> readAddressRisks(Path path, ObjectMapper objectMapper) throws IOException {
        JsonNode root = objectMapper.readTree(Files.readString(path));
        Map<String, AddressRiskRecord> risks = new LinkedHashMap<>();
        root.fields().forEachRemaining(entry -> risks.put(entry.getKey(), new AddressRiskRecord(
                entry.getValue().path("risk_score").asInt(),
                entry.getValue().path("category").asText())));
        return Map.copyOf(risks);
    }

    /**
     * 从文件系统中的汇率文件读取参考汇率。
     *
     * @param path 汇率文件路径
     * @param objectMapper JSON 解析器
     * @return 按交易对索引的参考汇率
     * @throws IOException 文件缺失或格式错误时抛出
     */
    /**
     * 从文件系统中的汇率文件读取参考汇率。
     *
     * @param path 汇率文件路径
     * @param objectMapper JSON 解析器
     * @return 按交易对索引的参考汇率
     * @throws IOException 文件缺失或格式错误时抛出
     */
    private static Map<String, BigDecimal> readReferenceRates(Path path, ObjectMapper objectMapper) throws IOException {
        JsonNode root = objectMapper.readTree(Files.readString(path));
        Map<String, BigDecimal> rates = new LinkedHashMap<>();
        root.fields().forEachRemaining(entry -> rates.put(entry.getKey(), entry.getValue().decimalValue()));
        return Map.copyOf(rates);
    }

    /**
     * 从类路径资源读取客户事实。
     *
     * @param resource 类路径资源路径
     * @param objectMapper JSON 解析器
     * @return 按客户标识索引的客户事实
     * @throws IOException 资源缺失或格式错误时抛出
     */
    /**
     * 从类路径资源读取客户事实。
     *
     * @param resource 类路径资源路径
     * @param objectMapper JSON 解析器
     * @return 按客户标识索引的客户事实
     * @throws IOException 资源缺失或格式错误时抛出
     */
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

    /**
     * 从类路径资源读取资产网络事实。
     *
     * @param resource 类路径资源路径
     * @param objectMapper JSON 解析器
     * @return 按 asset/network 索引的资产网络事实
     * @throws IOException 资源缺失或格式错误时抛出
     */
    /**
     * 从类路径资源读取资产网络事实。
     *
     * @param resource 类路径资源路径
     * @param objectMapper JSON 解析器
     * @return 按 asset/network 索引的资产网络事实
     * @throws IOException 资源缺失或格式错误时抛出
     */
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

    /**
     * 从类路径资源读取地址风险事实。
     *
     * @param resource 类路径资源路径
     * @param objectMapper JSON 解析器
     * @return 按地址索引的风险事实
     * @throws IOException 资源缺失或格式错误时抛出
     */
    /**
     * 从类路径资源读取地址风险事实。
     *
     * @param resource 类路径资源路径
     * @param objectMapper JSON 解析器
     * @return 按地址索引的风险事实
     * @throws IOException 资源缺失或格式错误时抛出
     */
    private static Map<String, AddressRiskRecord> readAddressRisks(String resource, ObjectMapper objectMapper) throws IOException {
        JsonNode root = readResourceTree(resource, objectMapper);
        Map<String, AddressRiskRecord> risks = new LinkedHashMap<>();
        root.fields().forEachRemaining(entry -> risks.put(entry.getKey(), new AddressRiskRecord(
                entry.getValue().path("risk_score").asInt(),
                entry.getValue().path("category").asText())));
        return Map.copyOf(risks);
    }

    /**
     * 从类路径资源读取参考汇率。
     *
     * @param resource 类路径资源路径
     * @param objectMapper JSON 解析器
     * @return 按交易对索引的参考汇率
     * @throws IOException 资源缺失或格式错误时抛出
     */
    /**
     * 从类路径资源读取参考汇率。
     *
     * @param resource 类路径资源路径
     * @param objectMapper JSON 解析器
     * @return 按交易对索引的参考汇率
     * @throws IOException 资源缺失或格式错误时抛出
     */
    private static Map<String, BigDecimal> readReferenceRates(String resource, ObjectMapper objectMapper) throws IOException {
        JsonNode root = readResourceTree(resource, objectMapper);
        Map<String, BigDecimal> rates = new LinkedHashMap<>();
        root.fields().forEachRemaining(entry -> rates.put(entry.getKey(), entry.getValue().decimalValue()));
        return Map.copyOf(rates);
    }

    /**
     * 读取类路径资源并解析为 JSON 树。
     *
     * @param resource 类路径资源路径
     * @param objectMapper JSON 解析器
     * @return 解析后的 JSON 树
     * @throws IOException 资源缺失或解析失败时抛出
     */
    /**
     * 读取类路径资源并解析为 JSON 树。
     *
     * @param resource 类路径资源路径
     * @param objectMapper JSON 解析器
     * @return 解析后的 JSON 树
     * @throws IOException 资源缺失或解析失败时抛出
     */
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
     * 查询订单声明资产和网络对应的配置。
     *
     * @param asset 资产代码
     * @param network 网络代码
     * @return 支持时返回资产网络配置，不支持时返回 null
     */
    public AssetNetworkRecord asset(String asset, String network) {
        return assets.get(assetKey(asset, network));
    }

    /**
     * 生成资产与网络的联合索引键。
     *
     * @param asset 资产代码
     * @param network 网络代码
     * @return 联合索引键
     */
    /**
     * 生成资产与网络的联合索引键。
     *
     * @param asset 资产代码
     * @param network 网络代码
     * @return 联合索引键
     */
    private static String assetKey(String asset, String network) {
        return asset + "/" + network;
    }
}
