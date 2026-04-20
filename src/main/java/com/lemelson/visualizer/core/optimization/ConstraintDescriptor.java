package com.lemelson.visualizer.core.optimization;

public record ConstraintDescriptor(
        String id,
        String description,
        ConstraintType type,
        double tolerance) {
}
