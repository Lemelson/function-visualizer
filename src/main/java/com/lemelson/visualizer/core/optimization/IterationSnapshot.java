package com.lemelson.visualizer.core.optimization;

import java.util.List;

public record IterationSnapshot(
        int iteration,
        List<AgentSnapshot> agents,
        int bestAgentIndex,
        double bestObjectiveValue,
        double bestPenalizedFitness,
        long elapsedMillis) {

    public IterationSnapshot {
        agents = List.copyOf(agents);
    }
}
