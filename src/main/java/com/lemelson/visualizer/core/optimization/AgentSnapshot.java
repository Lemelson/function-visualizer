package com.lemelson.visualizer.core.optimization;

import java.util.Arrays;

public record AgentSnapshot(
        int agentIndex,
        double[] position,
        double objectiveValue,
        double penalizedFitness,
        double totalViolation,
        boolean feasible) {

    public AgentSnapshot {
        position = Arrays.copyOf(position, position.length);
    }
}
