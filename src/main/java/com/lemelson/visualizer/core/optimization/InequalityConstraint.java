package com.lemelson.visualizer.core.optimization;

import java.util.Objects;
import java.util.function.ToDoubleFunction;

public final class InequalityConstraint extends AbstractConstraint {

    private final InequalityDirection direction;
    private final ToDoubleFunction<double[]> evaluator;

    public InequalityConstraint(String id, String description,
            InequalityDirection direction,
            ToDoubleFunction<double[]> evaluator) {
        super(id, description);
        this.direction = Objects.requireNonNull(direction, "direction");
        this.evaluator = Objects.requireNonNull(evaluator, "evaluator");
    }

    @Override
    public ConstraintType type() {
        return ConstraintType.INEQUALITY;
    }

    @Override
    public double tolerance() {
        return 0.0;
    }

    @Override
    public double rawValue(double[] point) {
        return evaluator.applyAsDouble(point);
    }

    @Override
    public double violation(double[] point) {
        double rawValue = rawValue(point);
        return switch (direction) {
            case LESS_OR_EQUAL -> Math.max(0.0, rawValue);
            case GREATER_OR_EQUAL -> Math.max(0.0, -rawValue);
        };
    }
}
