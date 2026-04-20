package com.lemelson.visualizer.core.optimization;

public interface ConstraintHandlingStrategy {

    double penalize(double objectiveValue, ConstraintSummary constraints);
}
