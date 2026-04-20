package com.lemelson.visualizer.core.optimization;

import com.lemelson.visualizer.core.benchmark.BenchmarkFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class OptimizationProblem {

    private final BenchmarkFunction function;
    private final List<Constraint> constraints;
    private final ConstraintHandlingStrategy penaltyStrategy;

    public OptimizationProblem(BenchmarkFunction function) {
        this(function, List.of(), new QuadraticPenaltyStrategy(1_000_000.0));
    }

    public OptimizationProblem(BenchmarkFunction function,
            List<Constraint> constraints,
            ConstraintHandlingStrategy penaltyStrategy) {
        this.function = Objects.requireNonNull(function, "function");
        this.constraints = List.copyOf(Objects.requireNonNull(constraints, "constraints"));
        this.penaltyStrategy = Objects.requireNonNull(penaltyStrategy, "penaltyStrategy");
    }

    public BenchmarkFunction function() {
        return function;
    }

    public List<Constraint> constraints() {
        return constraints;
    }

    public int dimension() {
        return function.dimension();
    }

    public double lowerBound(int index) {
        return function.lowerBound(index);
    }

    public double upperBound(int index) {
        return function.upperBound(index);
    }

    public CandidateEvaluation evaluate(double[] point) {
        double objectiveValue = function.evaluate(point);
        ConstraintSummary summary = evaluateConstraints(point);
        double penalizedFitness = penaltyStrategy.penalize(objectiveValue, summary);
        return new CandidateEvaluation(objectiveValue, penalizedFitness, summary);
    }

    public List<ConstraintDescriptor> constraintDescriptors() {
        return constraints.stream()
                .map(Constraint::descriptor)
                .toList();
    }

    private static final double FEASIBILITY_EPSILON = 1e-12;

    private ConstraintSummary evaluateConstraints(double[] point) {
        if (constraints.isEmpty()) {
            return ConstraintSummary.unconstrained();
        }
        List<ConstraintCheck> checks = new ArrayList<>(constraints.size());
        double totalViolation = 0.0;
        for (Constraint constraint : constraints) {
            double rawValue = constraint.rawValue(point);
            double violation = constraint.violation(point);
            totalViolation += violation;
            checks.add(new ConstraintCheck(constraint.id(), constraint.type(), rawValue, violation));
        }
        return new ConstraintSummary(checks, totalViolation, totalViolation <= FEASIBILITY_EPSILON);
    }
}
