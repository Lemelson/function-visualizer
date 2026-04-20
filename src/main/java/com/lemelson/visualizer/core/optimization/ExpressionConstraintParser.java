package com.lemelson.visualizer.core.optimization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public final class ExpressionConstraintParser {

    private static final Pattern VARIABLE_TOKEN = Pattern.compile("\\bx\\d+\\b");

    public List<Constraint> parse(String input, int dimension, double equalityTolerance) {
        if (dimension < 1) {
            throw new IllegalArgumentException("dimension must be >= 1");
        }
        if (input == null || input.isBlank()) {
            return List.of();
        }

        String[] variableNames = buildVariableNames(dimension);
        List<Constraint> result = new ArrayList<>();
        String[] statements = input.split("[;\\n]+");
        int constraintIndex = 1;
        for (String rawStatement : statements) {
            String statement = rawStatement.trim();
            if (statement.isEmpty()) {
                continue;
            }
            result.add(parseSingle(statement, constraintIndex++, variableNames, equalityTolerance));
        }
        return List.copyOf(result);
    }

    public Constraint parseOne(String statement, int dimension, int index, double equalityTolerance) {
        if (dimension < 1) {
            throw new IllegalArgumentException("dimension must be >= 1");
        }
        if (statement == null || statement.isBlank()) {
            throw new IllegalArgumentException("Пустое ограничение");
        }
        String[] variableNames = buildVariableNames(dimension);
        return parseSingle(statement.trim(), Math.max(1, index), variableNames, equalityTolerance);
    }

    private Constraint parseSingle(String statement, int index, String[] variableNames, double equalityTolerance) {
        String operator = extractOperator(statement);
        int splitIndex = statement.indexOf(operator);
        String left = statement.substring(0, splitIndex).trim();
        String right = statement.substring(splitIndex + operator.length()).trim();
        if (left.isEmpty() || right.isEmpty()) {
            throw new IllegalArgumentException("Некорректное ограничение: " + statement);
        }

        String deltaExpression = "(" + left + ") - (" + right + ")";
        Expression expression = buildExpression(deltaExpression, variableNames);

        Object lock = new Object();
        String constraintId = "c" + index;

        return switch (operator) {
            case "<=" -> new InequalityConstraint(
                    constraintId,
                    statement,
                    InequalityDirection.LESS_OR_EQUAL,
                    point -> {
                        synchronized (lock) {
                            return evaluate(expression, variableNames, point);
                        }
                    });
            case ">=" -> new InequalityConstraint(
                    constraintId,
                    statement,
                    InequalityDirection.GREATER_OR_EQUAL,
                    point -> {
                        synchronized (lock) {
                            return evaluate(expression, variableNames, point);
                        }
                    });
            case "=",
                 "==" -> new EqualityConstraint(
                    constraintId,
                    statement,
                    point -> {
                        synchronized (lock) {
                            return evaluate(expression, variableNames, point);
                        }
                    },
                    equalityTolerance);
            default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
        };
    }

    private static Expression buildExpression(String expressionText, String[] variableNames) {
        validateVariableTokens(expressionText, variableNames);
        Expression expression = new ExpressionBuilder(expressionText)
                .variables(variableNames)
                .build();
        validateVariables(expression.getVariableNames(), variableNames);
        return expression;
    }

    private static double evaluate(Expression expression, String[] variableNames, double[] point) {
        if (point.length < variableNames.length) {
            throw new IllegalArgumentException("Point dimension is smaller than expected");
        }
        for (int i = 0; i < variableNames.length; i++) {
            expression.setVariable(variableNames[i], point[i]);
        }
        return expression.evaluate();
    }

    private static void validateVariables(Set<String> usedVariables, String[] variableNames) {
        Set<String> allowed = toAllowedSet(variableNames);
        for (String variable : usedVariables) {
            if (!allowed.contains(variable)) {
                throw new IllegalArgumentException("Неизвестная переменная в ограничении: " + variable);
            }
        }
    }

    private static void validateVariableTokens(String expressionText, String[] variableNames) {
        Set<String> allowed = toAllowedSet(variableNames);
        Matcher matcher = VARIABLE_TOKEN.matcher(expressionText);
        while (matcher.find()) {
            String variable = matcher.group();
            if (!allowed.contains(variable)) {
                throw new IllegalArgumentException("Неизвестная переменная в ограничении: " + variable);
            }
        }
    }

    private static Set<String> toAllowedSet(String[] variableNames) {
        Set<String> allowed = new HashSet<>();
        for (String variableName : variableNames) {
            allowed.add(variableName);
        }
        return allowed;
    }

    private static String[] buildVariableNames(int dimension) {
        String[] names = new String[dimension];
        for (int i = 0; i < dimension; i++) {
            names[i] = "x" + (i + 1);
        }
        return names;
    }

    private static String extractOperator(String statement) {
        if (statement.contains("<=")) {
            return "<=";
        }
        if (statement.contains(">=")) {
            return ">=";
        }
        if (statement.contains("==")) {
            return "==";
        }
        if (statement.contains("=")) {
            return "=";
        }
        throw new IllegalArgumentException(
                "Ограничение должно содержать <=, >= или = : " + statement);
    }
}
