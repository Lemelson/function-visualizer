package com.lemelson.visualizer.core.grid;

import com.lemelson.visualizer.core.benchmark.BenchmarkFunction;
import java.util.Arrays;
import java.util.Objects;

public final class SliceDefinition {

    private final BenchmarkFunction function;
    private final int xIndex;
    private final int yIndex;
    private final double[] basePoint;

    public SliceDefinition(BenchmarkFunction function, int xIndex, int yIndex, double[] basePoint) {
        this.function = Objects.requireNonNull(function, "function");
        int dimension = function.dimension();
        if (xIndex < 0 || xIndex >= dimension) {
            throw new IllegalArgumentException("Недопустимый индекс измерения X: " + xIndex);
        }
        if (yIndex < 0 || yIndex >= dimension) {
            throw new IllegalArgumentException("Недопустимый индекс измерения Y: " + yIndex);
        }
        if (xIndex == yIndex) {
            throw new IllegalArgumentException("Оси X и Y должны ссылаться на разные измерения");
        }
        if (basePoint == null || basePoint.length != dimension) {
            throw new IllegalArgumentException("basePoint должен иметь длину " + dimension);
        }
        this.xIndex = xIndex;
        this.yIndex = yIndex;
        this.basePoint = Arrays.copyOf(basePoint, basePoint.length);
    }

    public BenchmarkFunction function() {
        return function;
    }

    public int xIndex() {
        return xIndex;
    }

    public int yIndex() {
        return yIndex;
    }

    public double[] createPointTemplate() {
        return Arrays.copyOf(basePoint, basePoint.length);
    }
}
