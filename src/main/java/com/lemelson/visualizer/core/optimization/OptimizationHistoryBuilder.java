package com.lemelson.visualizer.core.optimization;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class OptimizationHistoryBuilder {

    private final OptimizationHistoryMetadata metadata;
    private final List<IterationSnapshot> iterations = new ArrayList<>();

    public OptimizationHistoryBuilder(OptimizationHistoryMetadata metadata) {
        this.metadata = metadata;
    }

    public OptimizationHistoryMetadata metadata() {
        return metadata;
    }

    public void addIteration(int iteration, List<Agent> agents, long elapsedMillis) {
        List<AgentSnapshot> snapshots = new ArrayList<>(agents.size());
        for (int index = 0; index < agents.size(); index++) {
            Agent agent = agents.get(index);
            snapshots.add(new AgentSnapshot(
                    index,
                    agent.position(),
                    agent.objectiveValue(),
                    agent.penalizedFitness(),
                    agent.totalViolation(),
                    agent.feasible()));
        }
        AgentSnapshot best = snapshots.stream()
                .min(Comparator.comparingDouble(AgentSnapshot::penalizedFitness))
                .orElseThrow();
        iterations.add(new IterationSnapshot(
                iteration,
                snapshots,
                best.agentIndex(),
                best.objectiveValue(),
                best.penalizedFitness(),
                elapsedMillis));
    }

    public OptimizationHistoryDocument build() {
        return new OptimizationHistoryDocument(metadata, iterations);
    }
}
