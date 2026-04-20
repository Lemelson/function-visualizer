package com.lemelson.visualizer.core.benchmark;

import java.util.Objects;
import java.util.function.IntFunction;

public record BenchmarkFunctionDefinition(
        String id,
        String displayName,
        FunctionCategory category,
        int minDimension,
        int maxDimension,
        int defaultDimension,
        IntFunction<BenchmarkFunction> factory) {

    public BenchmarkFunctionDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(factory, "factory");
        if (minDimension < 1 || maxDimension < minDimension) {
            throw new IllegalArgumentException("Недопустимый диапазон размерностей: " + minDimension + "..." + maxDimension);
        }
        if (defaultDimension < minDimension || defaultDimension > maxDimension) {
            throw new IllegalArgumentException("defaultDimension вне диапазона допустимых значений");
        }
    }

    public boolean isDimensionFixed() {
        return minDimension == maxDimension;
    }

    public BenchmarkFunction create(int dimension) {
        if (dimension < minDimension || dimension > maxDimension) {
            throw new IllegalArgumentException("Размерность " + dimension + " вне диапазона "
                    + minDimension + "..." + maxDimension + " для функции " + id);
        }
        BenchmarkFunction function = factory.apply(dimension);
        if (function.dimension() != dimension) {
            throw new IllegalStateException("Фабрика функции " + id + " вернула экземпляр с размерностью "
                    + function.dimension() + " вместо " + dimension);
        }
        return function;
    }
}
