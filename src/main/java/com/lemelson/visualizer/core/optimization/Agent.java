package com.lemelson.visualizer.core.optimization;

import java.util.Arrays;
import java.util.Objects;

public final class Agent {

    private double[] position;
    private CandidateEvaluation evaluation;

    public Agent(double[] position, CandidateEvaluation evaluation) {
        this.position = copyPosition(position);
        this.evaluation = Objects.requireNonNull(evaluation, "evaluation");
    }

    public double[] position() {
        return copyPosition(position);
    }

    public CandidateEvaluation evaluation() {
        return evaluation;
    }

    public double objectiveValue() {
        return evaluation.objectiveValue();
    }

    public double penalizedFitness() {
        return evaluation.penalizedFitness();
    }

    public boolean feasible() {
        return evaluation.feasible();
    }

    public double totalViolation() {
        return evaluation.totalViolation();
    }

    public void replace(double[] newPosition, CandidateEvaluation newEvaluation) {
        this.position = copyPosition(newPosition);
        this.evaluation = Objects.requireNonNull(newEvaluation, "newEvaluation");
    }

    public Agent copy() {
        return new Agent(position, evaluation);
    }

    double[] rawPosition() {
        return position;
    }

    private static double[] copyPosition(double[] value) {
        Objects.requireNonNull(value, "position");
        return Arrays.copyOf(value, value.length);
    }
}
