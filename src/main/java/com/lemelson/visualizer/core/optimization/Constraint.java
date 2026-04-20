package com.lemelson.visualizer.core.optimization;

public interface Constraint {

    String id();

    String description();

    ConstraintType type();

    double tolerance();

    double rawValue(double[] point);

    double violation(double[] point);

    default boolean isSatisfied(double[] point) {
        return violation(point) <= 0.0;
    }

    default ConstraintDescriptor descriptor() {
        return new ConstraintDescriptor(id(), description(), type(), tolerance());
    }
}
