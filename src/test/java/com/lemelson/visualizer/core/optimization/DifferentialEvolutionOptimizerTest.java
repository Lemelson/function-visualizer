package com.lemelson.visualizer.core.optimization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lemelson.visualizer.core.benchmark.AbstractBenchmarkFunction;
import com.lemelson.visualizer.core.benchmark.BenchmarkFunction;
import org.junit.jupiter.api.Test;

class DifferentialEvolutionOptimizerTest {

    @Test
    void recordsInitialPopulationAndEveryIteration() {
        BenchmarkFunction sphere = new AbstractBenchmarkFunction(
                "T1", "Test Sphere", 2,
                new double[] {-5.0, -5.0},
                new double[] {5.0, 5.0},
                0.0,
                new double[] {0.0, 0.0}) {
            @Override
            public double evaluate(double[] x) {
                return x[0] * x[0] + x[1] * x[1];
            }
        };

        OptimizationProblem problem = new OptimizationProblem(sphere);
        DifferentialEvolutionOptimizer optimizer = new DifferentialEvolutionOptimizer(20, 15, 0.9, 0.7, 42L);

        OptimizationRunResult result = optimizer.optimize(problem);

        assertEquals(16, result.history().iterations().size());
        assertEquals(20, result.history().iterations().get(0).agents().size());
        assertTrue(result.history().iterations().get(15).bestPenalizedFitness()
                <= result.history().iterations().get(0).bestPenalizedFitness());
    }
}
