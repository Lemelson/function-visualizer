package com.lemelson.visualizer.core.optimization;

public record CandidateEvaluation(
        double objectiveValue,
        double penalizedFitness,
        ConstraintSummary constraintSummary) {

    public boolean feasible() {
        return constraintSummary.feasible();
    }

    public double totalViolation() {
        return constraintSummary.totalViolation();
    }
}
