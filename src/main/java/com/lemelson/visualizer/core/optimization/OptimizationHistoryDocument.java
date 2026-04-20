package com.lemelson.visualizer.core.optimization;

import java.util.List;

public record OptimizationHistoryDocument(
        OptimizationHistoryMetadata metadata,
        List<IterationSnapshot> iterations) {

    public OptimizationHistoryDocument {
        iterations = List.copyOf(iterations);
    }
}
