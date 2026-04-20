package com.lemelson.visualizer.core.optimization;

import java.util.Map;

public interface Optimizer {

    String name();

    Map<String, Double> parameters();

    OptimizationRunResult optimize(OptimizationProblem problem);
}
