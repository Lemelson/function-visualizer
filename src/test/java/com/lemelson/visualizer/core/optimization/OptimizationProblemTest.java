package com.lemelson.visualizer.core.optimization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lemelson.visualizer.core.benchmark.AbstractBenchmarkFunction;
import com.lemelson.visualizer.core.benchmark.BenchmarkFunction;
import java.util.List;
import org.junit.jupiter.api.Test;

class OptimizationProblemTest {

    @Test
    void evaluatesEqualityAndInequalityConstraints() {
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

        OptimizationProblem problem = new OptimizationProblem(
                sphere,
                List.of(
                        new InequalityConstraint("g1", "x1 must be >= 1", InequalityDirection.GREATER_OR_EQUAL, x -> x[0] - 1.0),
                        new EqualityConstraint("h1", "x2 must be close to 2", x -> x[1] - 2.0, 0.1)),
                new QuadraticPenaltyStrategy(100.0));

        CandidateEvaluation feasible = problem.evaluate(new double[] {1.5, 2.05});
        assertTrue(feasible.feasible());
        assertEquals(0.0, feasible.totalViolation(), 1e-12);

        CandidateEvaluation infeasible = problem.evaluate(new double[] {0.3, 1.6});
        assertFalse(infeasible.feasible());
        assertTrue(infeasible.totalViolation() > 0.0);
        assertTrue(infeasible.penalizedFitness() > infeasible.objectiveValue());
    }
}
