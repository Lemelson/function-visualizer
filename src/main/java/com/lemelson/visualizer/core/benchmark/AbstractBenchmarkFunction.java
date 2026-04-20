package com.lemelson.visualizer.core.benchmark;

import java.util.Arrays;
import java.util.Optional;

public abstract class AbstractBenchmarkFunction implements BenchmarkFunction {

    private final String id;
    private final String displayName;
    private final int dimension;
    private final double[] lowerBounds;
    private final double[] upperBounds;
    private final double globalMinimum;
    private final double[] globalMinimumPoint;

    protected AbstractBenchmarkFunction(String id,
                                        String displayName,
                                        int dimension,
                                        double[] lowerBounds,
                                        double[] upperBounds,
                                        double globalMinimum,
                                        double[] globalMinimumPoint) {
        if (dimension < 1) {
            throw new IllegalArgumentException("dimension must be >= 1");
        }
        this.id = id;
        this.displayName = displayName;
        this.dimension = dimension;
        this.lowerBounds = validateBoundsArray(lowerBounds, dimension, "lowerBounds");
        this.upperBounds = validateBoundsArray(upperBounds, dimension, "upperBounds");
        this.globalMinimum = globalMinimum;
        this.globalMinimumPoint = globalMinimumPoint != null
                ? copyAndValidate(globalMinimumPoint, dimension, "globalMinimumPoint")
                : null;
    }

    private static double[] validateBoundsArray(double[] bounds, int dimension, String name) {
        if (bounds == null || bounds.length != dimension) {
            throw new IllegalArgumentException(name + " must have length " + dimension);
        }
        double[] copy = Arrays.copyOf(bounds, bounds.length);
        for (double v : copy) {
            if (!Double.isFinite(v)) {
                throw new IllegalArgumentException(name + " must contain finite values");
            }
        }
        return copy;
    }

    private static double[] copyAndValidate(double[] point, int dimension, String name) {
        if (point.length != dimension) {
            throw new IllegalArgumentException(name + " must have length " + dimension);
        }
        return Arrays.copyOf(point, point.length);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String displayName() {
        return displayName;
    }

    @Override
    public int dimension() {
        return dimension;
    }

    @Override
    public double lowerBound(int index) {
        return lowerBounds[index];
    }

    @Override
    public double upperBound(int index) {
        return upperBounds[index];
    }

    @Override
    public double globalMinimumValue() {
        return globalMinimum;
    }

    @Override
    public Optional<double[]> globalMinimumPoint() {
        return globalMinimumPoint == null ? Optional.empty() : Optional.of(Arrays.copyOf(globalMinimumPoint, globalMinimumPoint.length));
    }

    protected double[] lowerBounds() {
        return Arrays.copyOf(lowerBounds, lowerBounds.length);
    }

    protected double[] upperBounds() {
        return Arrays.copyOf(upperBounds, upperBounds.length);
    }

    protected static double[] uniformArray(int dimension, double value) {
        double[] arr = new double[dimension];
        Arrays.fill(arr, value);
        return arr;
    }

    protected static double[] zeros(int dimension) {
        return new double[dimension];
    }
}
