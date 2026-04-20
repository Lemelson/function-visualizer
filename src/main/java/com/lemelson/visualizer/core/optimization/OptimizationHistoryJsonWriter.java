package com.lemelson.visualizer.core.optimization;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class OptimizationHistoryJsonWriter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String toJson(OptimizationHistoryDocument history) throws IOException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(history);
    }

    public void write(OptimizationHistoryDocument history, Path target) throws IOException {
        Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(target.toFile(), history);
    }
}
