package com.lemelson.visualizer.core.benchmark;

import java.util.Optional;

public interface BenchmarkFunction {

    String id();

    String displayName();

    int dimension();

    double lowerBound(int index);

    double upperBound(int index);

    double globalMinimumValue();

    default Optional<double[]> globalMinimumPoint() {
        return Optional.empty();
    }

    double evaluate(double[] x);
}
