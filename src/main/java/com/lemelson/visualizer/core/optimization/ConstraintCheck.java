package com.lemelson.visualizer.core.optimization;

public record ConstraintCheck(
        String constraintId,
        ConstraintType type,
        double rawValue,
        double violation) {
}
