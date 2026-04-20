package com.lemelson.visualizer.core.optimization;

import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

public abstract class AbstractPopulationOptimizer implements Optimizer {

    private final String name;
    private final int populationSize;
    private final int maxIterations;
    protected final Random random;

    protected AbstractPopulationOptimizer(String name, int populationSize, int maxIterations, long seed) {
        if (populationSize < 4) {
            throw new IllegalArgumentException("populationSize must be >= 4");
        }
        if (maxIterations < 1) {
            throw new IllegalArgumentException("maxIterations must be >= 1");
        }
        this.name = name;
        this.populationSize = populationSize;
        this.maxIterations = maxIterations;
        this.random = new Random(seed);
    }

    @Override
    public final String name() {
        return name;
    }

    public final int populationSize() {
        return populationSize;
    }

    public final int maxIterations() {
        return maxIterations;
    }

    protected double[] randomPosition(OptimizationProblem problem) {
        double[] point = new double[problem.dimension()];
        for (int i = 0; i < point.length; i++) {
            double min = problem.lowerBound(i);
            double max = problem.upperBound(i);
            point[i] = min + random.nextDouble() * (max - min);
        }
        return point;
    }

    protected void clipToBounds(double[] point, OptimizationProblem problem) {
        for (int i = 0; i < point.length; i++) {
            point[i] = clamp(point[i], problem.lowerBound(i), problem.upperBound(i));
        }
    }

    protected int[] sampleDistinctIndices(int excluded, int upperExclusive, int count) {
        Set<Integer> values = new LinkedHashSet<>();
        while (values.size() < count) {
            int candidate = random.nextInt(upperExclusive);
            if (candidate != excluded) {
                values.add(candidate);
            }
        }
        return values.stream().mapToInt(Integer::intValue).toArray();
    }

    protected OptimizationHistoryMetadata buildMetadata(OptimizationProblem problem) {
        return new OptimizationHistoryMetadata(
                name(),
                problem.function().id(),
                problem.function().displayName(),
                problem.dimension(),
                populationSize(),
                maxIterations(),
                parameters(),
                problem.constraintDescriptors());
    }

    protected AgentSnapshot bestAgentSnapshot(OptimizationHistoryDocument history) {
        IterationSnapshot lastIteration = history.iterations().get(history.iterations().size() - 1);
        return lastIteration.agents().get(lastIteration.bestAgentIndex());
    }

    private static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
