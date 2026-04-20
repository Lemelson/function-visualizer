package com.lemelson.visualizer.core.grid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lemelson.visualizer.core.benchmark.BenchmarkFunction;
import java.util.Optional;
import java.util.function.DoubleBinaryOperator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

class GridComputerTest {

    @Test
    void computesPlaneFunction() {
        GridComputer computer = new GridComputer();
        GridSpec spec = new GridSpec(0, 1, 0, 1, 2, 2);
        SliceDefinition slice = new SliceDefinition(testFunction((x, y) -> x + y), 0, 1, new double[] {0.0, 0.0});
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            double[][] grid = computer.compute(slice, spec, pool);
            assertEquals(0.0, grid[0][0], 1e-9);
            assertEquals(1.0, grid[0][1], 1e-9);
            assertEquals(1.0, grid[1][0], 1e-9);
            assertEquals(2.0, grid[1][1], 1e-9);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void replacesNonFiniteValuesWithNaN() {
        GridComputer computer = new GridComputer();
        GridSpec spec = new GridSpec(0, 1, 0, 1, 2, 2);
        SliceDefinition slice =
                new SliceDefinition(testFunction((x, y) -> Double.POSITIVE_INFINITY), 0, 1, new double[] {0.0, 0.0});
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            double[][] grid = computer.compute(slice, spec, pool);
            assertTrue(Double.isNaN(grid[0][0]));
            assertTrue(Double.isNaN(grid[1][1]));
        } finally {
            pool.shutdownNow();
        }
    }

    private BenchmarkFunction testFunction(DoubleBinaryOperator op) {
        return new BenchmarkFunction() {
            @Override
            public String id() {
                return "TEST";
            }

            @Override
            public String displayName() {
                return "Test function";
            }

            @Override
            public int dimension() {
                return 2;
            }

            @Override
            public double lowerBound(int index) {
                return -Double.MAX_VALUE;
            }

            @Override
            public double upperBound(int index) {
                return Double.MAX_VALUE;
            }

            @Override
            public double globalMinimumValue() {
                return 0.0;
            }

            @Override
            public Optional<double[]> globalMinimumPoint() {
                return Optional.empty();
            }

            @Override
            public double evaluate(double[] x) {
                return op.applyAsDouble(x[0], x[1]);
            }
        };
    }
}
