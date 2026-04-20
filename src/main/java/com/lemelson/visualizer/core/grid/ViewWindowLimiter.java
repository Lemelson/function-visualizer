package com.lemelson.visualizer.core.grid;

import com.lemelson.visualizer.core.benchmark.BenchmarkFunction;
import java.util.Objects;

public final class ViewWindowLimiter {

    private static final double DOMAIN_PADDING_FACTOR = 32.0;
    private static final double MIN_SPAN_RATIO = 1e-6;
    private static final double MIN_ABSOLUTE_SPAN = 1e-12;

    private ViewWindowLimiter() {
    }

    public static double allowedLowerBound(BenchmarkFunction function, int dimension) {
        Objects.requireNonNull(function, "function");
        double domainSpan = domainSpan(function, dimension);
        return function.lowerBound(dimension) - domainSpan * DOMAIN_PADDING_FACTOR;
    }

    public static double allowedUpperBound(BenchmarkFunction function, int dimension) {
        Objects.requireNonNull(function, "function");
        double domainSpan = domainSpan(function, dimension);
        return function.upperBound(dimension) + domainSpan * DOMAIN_PADDING_FACTOR;
    }

    public static ViewWindow limit(BenchmarkFunction function, int xDimension, int yDimension,
            double desiredXMin, double desiredXMax, double desiredYMin, double desiredYMax) {
        Objects.requireNonNull(function, "function");
        AxisWindow xWindow = limitAxis(desiredXMin, desiredXMax,
                function.lowerBound(xDimension), function.upperBound(xDimension));
        AxisWindow yWindow = limitAxis(desiredYMin, desiredYMax,
                function.lowerBound(yDimension), function.upperBound(yDimension));
        return new ViewWindow(xWindow.min(), xWindow.max(), yWindow.min(), yWindow.max());
    }

    private static AxisWindow limitAxis(double desiredMin, double desiredMax, double domainMin, double domainMax) {
        if (!Double.isFinite(desiredMin) || !Double.isFinite(desiredMax)) {
            throw new IllegalArgumentException("Границы окна должны быть конечными");
        }
        if (!Double.isFinite(domainMin) || !Double.isFinite(domainMax) || domainMax <= domainMin) {
            throw new IllegalArgumentException("Границы домена должны быть корректными");
        }

        double domainSpan = Math.max(domainMax - domainMin, MIN_ABSOLUTE_SPAN);
        double minSpan = Math.min(Math.max(domainSpan * MIN_SPAN_RATIO, MIN_ABSOLUTE_SPAN),
                domainSpan * (1.0 + DOMAIN_PADDING_FACTOR * 2.0));
        double allowedMin = domainMin - domainSpan * DOMAIN_PADDING_FACTOR;
        double allowedMax = domainMax + domainSpan * DOMAIN_PADDING_FACTOR;
        double maxSpan = allowedMax - allowedMin;

        double desiredSpan = desiredMax - desiredMin;
        double span = clamp(desiredSpan, minSpan, maxSpan);
        double center = desiredMin + desiredSpan / 2.0;

        double minCenter = allowedMin + span / 2.0;
        double maxCenter = allowedMax - span / 2.0;
        center = clamp(center, minCenter, maxCenter);

        return new AxisWindow(center - span / 2.0, center + span / 2.0);
    }

    private static double domainSpan(BenchmarkFunction function, int dimension) {
        return Math.max(function.upperBound(dimension) - function.lowerBound(dimension), MIN_ABSOLUTE_SPAN);
    }

    private static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private record AxisWindow(double min, double max) {
    }
}
