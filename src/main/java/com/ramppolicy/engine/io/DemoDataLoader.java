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
 * Loads the vendored demo inputs from the repository.
 */
public final class DemoDataLoader {

    private final JsonlOrderReader orderReader;

    public DemoDataLoader(ObjectMapper objectMapper) {
        this.orderReader = new JsonlOrderReader(objectMapper);
    }

    /**
     * Loads orders from a filesystem path.
     *
     * @param path JSONL file path
     * @return parsed orders
     * @throws IOException when the file cannot be read
     */
    public List<OrderRecord> loadOrders(Path path) throws IOException {
        return orderReader.readAll(Files.readString(path, StandardCharsets.UTF_8));
    }

    /**
     * Loads orders from a classpath resource.
     *
     * @param resource classpath resource path
     * @return parsed orders
     * @throws IOException when the resource is missing or unreadable
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
