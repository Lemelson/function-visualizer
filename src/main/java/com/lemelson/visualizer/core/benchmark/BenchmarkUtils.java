package com.lemelson.visualizer.core.benchmark;

import java.util.Arrays;

final class BenchmarkUtils {

    private BenchmarkUtils() {
    }

    static double[] uniform(int dimension, double value) {
        double[] arr = new double[dimension];
        Arrays.fill(arr, value);
        return arr;
    }

    static double[] zeros(int dimension) {
        return new double[dimension];
    }
}
