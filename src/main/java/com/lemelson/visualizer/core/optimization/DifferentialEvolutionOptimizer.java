package com.lemelson.visualizer.core.optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class DifferentialEvolutionOptimizer extends AbstractPopulationOptimizer {

    private final double crossoverProbability;
    private final double differentialWeight;
    private final long seed;

    public DifferentialEvolutionOptimizer(
            int populationSize,
            int maxIterations,
            double crossoverProbability,
            double differentialWeight,
            long seed) {
        super("DifferentialEvolution", populationSize, maxIterations, seed);
        if (!Double.isFinite(crossoverProbability) || crossoverProbability < 0.0 || crossoverProbability > 1.0) {
            throw new IllegalArgumentException("crossoverProbability must be in [0, 1]");
        }
        if (!Double.isFinite(differentialWeight) || differentialWeight < 0.0 || differentialWeight > 2.0) {
            throw new IllegalArgumentException("differentialWeight must be in [0, 2]");
        }
        this.crossoverProbability = crossoverProbability;
        this.differentialWeight = differentialWeight;
        this.seed = seed;
    }

    @Override
    public Map<String, Double> parameters() {
        return Map.of(
                "populationSize", (double) populationSize(),
                "maxIterations", (double) maxIterations(),
                "crossoverProbability", crossoverProbability,
                "differentialWeight", differentialWeight,
                "seed", (double) seed);
    }

    @Override
    public OptimizationRunResult optimize(OptimizationProblem problem) {
        List<Agent> population = initializePopulation(problem);
        OptimizationHistoryBuilder history = new OptimizationHistoryBuilder(buildMetadata(problem));
        long startedAt = System.nanoTime();
        history.addIteration(0, population, elapsedMillis(startedAt));

        for (int iteration = 1; iteration <= maxIterations(); iteration++) {

            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            for (int i = 0; i < population.size(); i++) {
                Agent target = population.get(i);
                int[] picks = sampleDistinctIndices(i, population.size(), 3);

                double[] trial = mutate(
                        target.rawPosition(),
                        population.get(picks[0]).rawPosition(),
                        population.get(picks[1]).rawPosition(),
                        population.get(picks[2]).rawPosition());
                clipToBounds(trial, problem);

                CandidateEvaluation trialEvaluation = problem.evaluate(trial);
                if (trialEvaluation.penalizedFitness() < target.penalizedFitness()) {
                    target.replace(trial, trialEvaluation);
                }
            }
            history.addIteration(iteration, population, elapsedMillis(startedAt));
        }

        OptimizationHistoryDocument document = history.build();
        return new OptimizationRunResult(bestAgentSnapshot(document), document);
    }

    private List<Agent> initializePopulation(OptimizationProblem problem) {
        List<Agent> population = new ArrayList<>(populationSize());
        for (int i = 0; i < populationSize(); i++) {
            double[] position = randomPosition(problem);
            population.add(new Agent(position, problem.evaluate(position)));
        }
        return population;
    }

    private double[] mutate(double[] target, double[] alpha, double[] beta, double[] gamma) {
        double[] trial = target.clone();
        int forceIndex = random.nextInt(target.length);
        for (int j = 0; j < target.length; j++) {
            if (random.nextDouble() < crossoverProbability || j == forceIndex) {
                trial[j] = alpha[j] + differentialWeight * (beta[j] - gamma[j]);
            }
        }
        return trial;
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000L;
    }
}
