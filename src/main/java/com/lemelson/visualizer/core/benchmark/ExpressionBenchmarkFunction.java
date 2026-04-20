package com.lemelson.visualizer.core.benchmark;

import java.util.HashSet;
import java.util.Set;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class ExpressionBenchmarkFunction extends AbstractBenchmarkFunction {

    private final String expressionText;
    private final String[] variables;
    private final Expression expression;
    private final Object lock = new Object();

    public ExpressionBenchmarkFunction(String expressionText,
            int dimension,
            double lowerBound,
            double upperBound) {
        super("CUSTOM",
                expressionText,
                dimension,
                BenchmarkUtils.uniform(dimension, lowerBound),
                BenchmarkUtils.uniform(dimension, upperBound),
                Double.NaN,
                null);
        if (expressionText == null || expressionText.isBlank()) {
            throw new IllegalArgumentException("Формула не должна быть пустой");
        }
        if (upperBound <= lowerBound) {
            throw new IllegalArgumentException("Верхняя граница должна быть больше нижней");
        }

        this.expressionText = expressionText;
        this.variables = buildVariableNames(dimension);

        this.expression = buildExpression();
        validateVariables(this.expression.getVariableNames());

        evaluate(new double[dimension]);
    }

    private String[] buildVariableNames(int dimension) {
        String[] names = new String[dimension];
        for (int i = 0; i < dimension; i++) {
            names[i] = "x" + (i + 1);
        }
        return names;
    }

    private Expression buildExpression() {
        return new ExpressionBuilder(expressionText)
                .variables(variables)
                .build();
    }

    private void validateVariables(Set<String> usedVariables) {
        Set<String> allowed = new HashSet<>();
        for (String v : variables) {
            allowed.add(v);
        }
        for (String variable : usedVariables) {
            if (!allowed.contains(variable)) {
                throw new IllegalArgumentException("Неизвестная переменная: " + variable);
            }
        }
    }

    @Override
    public double evaluate(double[] x) {
        double result;

        synchronized (lock) {
            for (int i = 0; i < variables.length; i++) {
                expression.setVariable(variables[i], x[i]);
            }
            result = expression.evaluate();
        }
        if (Double.isNaN(result) || Double.isInfinite(result)) {
            return Double.NaN;
        }
        return result;
    }

    public String expressionText() {
        return expressionText;
    }
}
