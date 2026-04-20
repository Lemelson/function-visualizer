package com.lemelson.visualizer.core.optimization;

import java.util.List;

public record ConstraintSummary(
        List<ConstraintCheck> checks,
        double totalViolation,
        boolean feasible) {

    public ConstraintSummary {
        checks = List.copyOf(checks);
    }

    public static ConstraintSummary unconstrained() {
        return new ConstraintSummary(List.of(), 0.0, true);
    }
}
