package com.lemelson.visualizer.core.optimization;

public record OptimizationRunResult(
        AgentSnapshot bestAgent,
        OptimizationHistoryDocument history) {
}
