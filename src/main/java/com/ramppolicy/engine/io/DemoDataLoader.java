package com.ramppolicy.engine.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ramppolicy.engine.domain.OrderRecord;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 加载仓库内置 Demo 输入数据。
 */
public final class DemoDataLoader {

    private final JsonlOrderReader orderReader;

    public DemoDataLoader(ObjectMapper objectMapper) {
        this.orderReader = new JsonlOrderReader(objectMapper);
    }

    /**
     * 从文件系统路径加载订单。
     *
     * @param path JSONL 文件路径
     * @return 解析后的订单列表
     * @throws IOException 文件无法读取时抛出
     */
    public List<OrderRecord> loadOrders(Path path) throws IOException {
        return orderReader.readAll(Files.readString(path, StandardCharsets.UTF_8));
    }

    /**
     * 从类路径资源加载订单。
     *
     * @param resource 类路径资源路径
     * @return 解析后的订单列表
     * @throws IOException 资源缺失或无法读取时抛出
     */
    public List<OrderRecord> loadOrdersFromClasspath(String resource) throws IOException {
        ClassLoader classLoader = DemoDataLoader.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(resource)) {
            if (inputStream == null) {
                throw new IOException("Missing classpath resource: " + resource);
            }
            return orderReader.readAll(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
