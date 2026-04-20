package com.lemelson.visualizer.core.optimization;

public final class QuadraticPenaltyStrategy implements ConstraintHandlingStrategy {

    private final double penaltyFactor;

    public QuadraticPenaltyStrategy(double penaltyFactor) {
        if (!Double.isFinite(penaltyFactor) || penaltyFactor <= 0.0) {
            throw new IllegalArgumentException("penaltyFactor must be finite and > 0");
        }
        this.penaltyFactor = penaltyFactor;
    }

    public double penaltyFactor() {
        return penaltyFactor;
    }

    @Override
    public double penalize(double objectiveValue, ConstraintSummary constraints) {
        if (!Double.isFinite(objectiveValue)) {
            return Double.POSITIVE_INFINITY;
        }
        if (constraints.feasible()) {
            return objectiveValue;
        }
        double violation = constraints.totalViolation();
        return objectiveValue + penaltyFactor * (violation + violation * violation);
    }
}
