package com.lemelson.visualizer.core.optimization;

import java.util.Objects;
import java.util.function.ToDoubleFunction;

public final class EqualityConstraint extends AbstractConstraint {

    private final ToDoubleFunction<double[]> evaluator;
    private final double epsilon;

    public EqualityConstraint(String id, String description,
            ToDoubleFunction<double[]> evaluator,
            double epsilon) {
        super(id, description);
        if (!Double.isFinite(epsilon) || epsilon < 0.0) {
            throw new IllegalArgumentException("epsilon must be finite and >= 0");
        }
        this.evaluator = Objects.requireNonNull(evaluator, "evaluator");
        this.epsilon = epsilon;
    }

    @Override
    public ConstraintType type() {
        return ConstraintType.EQUALITY;
    }

    @Override
    public double tolerance() {
        return epsilon;
    }

    @Override
    public double rawValue(double[] point) {
        return evaluator.applyAsDouble(point);
    }

    @Override
    public double violation(double[] point) {
        return Math.max(0.0, Math.abs(rawValue(point)) - epsilon);
    }
}
