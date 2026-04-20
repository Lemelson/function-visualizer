package com.lemelson.visualizer.core.grid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lemelson.visualizer.core.benchmark.BenchmarkFunction;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ViewWindowLimiterTest {

    @Test
    void limitsExtremeZoomOutToExtendedDomain() {
        ViewWindow limited = ViewWindowLimiter.limit(testFunction(), 0, 1,
                -1_000_000.0, 1_000_000.0, -2_000_000.0, 2_000_000.0);

        assertEquals(-325.0, limited.xmin(), 1e-9);
        assertEquals(325.0, limited.xmax(), 1e-9);
        assertEquals(-650.0, limited.ymin(), 1e-9);
        assertEquals(650.0, limited.ymax(), 1e-9);
    }

    @Test
    void limitsExtremeZoomInToMinimumSpan() {
        ViewWindow limited = ViewWindowLimiter.limit(testFunction(), 0, 1,
                1.0, 1.0 + 1e-12, -2.0, -2.0 + 1e-12);

        assertTrue(limited.xmax() - limited.xmin() >= 1e-5);
        assertTrue(limited.ymax() - limited.ymin() >= 2e-5);
    }

    @Test
    void keepsCenterInsideAllowedPanArea() {
        ViewWindow limited = ViewWindowLimiter.limit(testFunction(), 0, 1,
                900.0, 920.0, -900.0, -880.0);

        assertEquals(305.0, limited.xmin(), 1e-9);
        assertEquals(325.0, limited.xmax(), 1e-9);
        assertEquals(-650.0, limited.ymin(), 1e-9);
        assertEquals(-630.0, limited.ymax(), 1e-9);
    }

    private BenchmarkFunction testFunction() {
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
                return index == 0 ? -5.0 : -10.0;
            }

            @Override
            public double upperBound(int index) {
                return index == 0 ? 5.0 : 10.0;
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
                return x[0] + x[1];
            }
        };
    }
}
