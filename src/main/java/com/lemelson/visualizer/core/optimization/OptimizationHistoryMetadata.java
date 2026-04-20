package com.lemelson.visualizer.core.optimization;

import java.util.List;
import java.util.Map;

public record OptimizationHistoryMetadata(
        String algorithm,
        String functionId,
        String functionName,
        int dimension,
        int populationSize,
        int maxIterations,
        Map<String, Double> algorithmParameters,
        List<ConstraintDescriptor> constraints) {

    public OptimizationHistoryMetadata {
        algorithmParameters = Map.copyOf(algorithmParameters);
        constraints = List.copyOf(constraints);
    }
}
